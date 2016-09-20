package de.chuparch0pper.android.xposed.pogoiv;

import android.app.AndroidAppHelper;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.github.aeonlucid.pogoprotos.Enums;
import com.github.aeonlucid.pogoprotos.inventory.Item;
import com.github.aeonlucid.pogoprotos.networking.Responses;
import com.google.protobuf.Descriptors;
import com.google.protobuf.ProtocolMessageEnum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XposedBridge;

class BubblestratPokemon {
    private int pokeNr;
    private double maxLevel;
    private int moveNr;

    public BubblestratPokemon(int pokeNr, double maxLevel, int moveNr) {
        this.pokeNr = pokeNr;
        this.maxLevel = maxLevel;
        this.moveNr = moveNr;
    }

    public int getPokeNr() {
        return pokeNr;
    }

    public double getMaxLevel() {
        return maxLevel;
    }

    public int getMoveNr() { return  moveNr; }
}

public class Helper {
    public static final String PACKAGE_NAME = IVChecker.class.getPackage().getName();

    private static Context context = null;
    private static Context pokeContext = null;

    private static String[] pokemonNames = null;
    private static BubblestratPokemon[] bubblestratPokemons = null;

    public static void Log(String message) {
        if (BuildConfig.DEBUG) {
            XposedBridge.log(message);
        }
    }

    public static void Log(String message, Set<Map.Entry<Descriptors.FieldDescriptor, Object>> entries) {
        for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : entries) {
            Helper.Log(message + entry.getKey() + " - " + entry.getValue());
        }

    }

    public static void showToast(final CharSequence message, final int length) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getPokeContext(), message, length).show();
            }
        });
    }

    public static void showNotification(final String title, final String text, final String longText) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                /*
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getPokeContext());
                mBuilder.setSmallIcon(android.R.drawable.ic_dialog_info);
                mBuilder.setContentTitle(title);
                mBuilder.setContentText(text);
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(longText));
                mBuilder.setVibrate(new long[]{1000});
                mBuilder.setPriority(Notification.PRIORITY_MAX);

                Intent showToastIntent = new Intent();
                showToastIntent.putExtra("longText", longText);
                showToastIntent.setAction(NotificationReceiver.TOAST);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getPokeContext(), 0, showToastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pendingIntent);

                NotificationManager mNotificationManager = (NotificationManager) getPokeContext().getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(699511, mBuilder.build());
                 */

                Intent intent = new Intent();
                intent.setAction(NotificationReceiver.SHOW_NOTIFICATION);
                intent.putExtra("title", title);
                intent.putExtra("text", text);
                intent.putExtra("longText", longText);
                getPokeContext().sendBroadcast(intent);

            }
        });
    }

    /**
     * Gets Context of this module
     *
     * @return Context of "de.chuparch0pper.android.xposed.pogoiv"
     */
    public static Context getContext() {
        if (context == null) {
            try {
                context = AndroidAppHelper.currentApplication().createPackageContext(PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);
            } catch (PackageManager.NameNotFoundException e) {
                Helper.Log("Could not get Context " + e);
            }
        }
        return context;
    }

    /**
     * Returns the main {@link android.app.Application} object in the current process.
     *
     * @return should be Context of "com.nianticlabs.pokemongo"
     */
    public static Context getPokeContext() {
        if (pokeContext == null) {
            pokeContext = AndroidAppHelper.currentApplication();
        }
        return pokeContext;
    }

    public static void loadPokemonNames() {
        pokemonNames = getContext().getResources().getStringArray(R.array.Pokemon);
    }

    public static String[] getPokemonNames() {
        if (pokemonNames == null) {
            loadPokemonNames();
        }
        return pokemonNames;
    }

    public static String getPokemonName(int pokemonNumber) {
        String[] pokemonNames = Helper.getPokemonNames();
        if (pokemonNumber > 0 && pokemonNumber <= pokemonNames.length)
            return Helper.getPokemonNames()[pokemonNumber - 1];
        else
            return "(unknown Pokémon: " + pokemonNumber + ")";
    }

    public static String getPokeMoveName(Enums.PokemonMove pokeMove) {
        // switch (pokeMove) {} // TODO later.. there are more than 300 moves
        return prettyPrintEnum(pokeMove.toString().replaceAll("_FAST$", ""));
    }


    public static String getCatchName(Responses.CatchPokemonResponse.CatchStatus status) {
        return prettyPrintEnum(status.toString());
    }

    public static String getItemName(Item.ItemId item) {
        return prettyPrintEnum(item.toString().replaceAll("^ITEM_", ""));
    }

    public static String getItemName(Item.ItemId item, int count) {
        String name = getItemName(item);
        if (count != 1)
            name += " (x" + count + ")";
        return name;
    }

    public static String getItemName(Item.ItemAward itemAward) {
        return getItemName(itemAward.getItemId(), itemAward.getItemCount());
    }

    public static String getGenericEnumName(ProtocolMessageEnum enumEntry) {
        return prettyPrintEnum(enumEntry.toString());
    }

    private static String prettyPrintEnum(String enums) {
        String pokeMoveName = "";
        String[] splitPokeMoveNames = enums.split("_");
        for (String stringPart : splitPokeMoveNames) {
            pokeMoveName += stringPart.charAt(0) + stringPart.substring(1).toLowerCase() + " ";
        }
        return pokeMoveName.trim();
    }


    public static String getCpName() {
        return getContext().getResources().getString(R.string.cp);
    }


    private static String loadJSONFromAssetBS() {
        try {
            InputStream inputStream = getContext().getAssets().open("bubblestrat.json");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            return new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
//    private static String loadJSONFromAssetML() {
//        try {
//            InputStream inputStream = getContext().getAssets().open("movelist.json");
//            byte[] buffer = new byte[inputStream.available()];
//            inputStream.read(buffer);
//            inputStream.close();
//            return new String(buffer, "UTF-8");
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    private static void loadPokemonMoves() {
//        try {
//            JSONObject jsonObject = new JSONObject(loadJSONFromAssetBS());
//            JSONArray jsonArray = jsonObject.getJSONArray("defenders");
//
//            bubblestratPokemons = new BubblestratPokemon[jsonArray.length()];
//            for (int i = 0; i < jsonArray.length(); i++) {
//                JSONObject jsonObjectInArray = jsonArray.getJSONObject(i);
//                bubblestratPokemons[i] = new BubblestratPokemon(jsonObjectInArray.getInt("id"), jsonObjectInArray.getDouble("max_level"), jsonObjectInArray.getInt("move"));
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//    }

    private static void loadBubblestratPokemon() {
        try {
            JSONObject jsonObject = new JSONObject(loadJSONFromAssetBS());
            JSONArray jsonArray = jsonObject.getJSONArray("defenders");

            bubblestratPokemons = new BubblestratPokemon[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObjectInArray = jsonArray.getJSONObject(i);
                bubblestratPokemons[i] = new BubblestratPokemon(jsonObjectInArray.getInt("id"), jsonObjectInArray.getDouble("max_level"), jsonObjectInArray.getInt("move"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static boolean isBubblestratPokemon(int pokemonId, double level, int moveID) {
        double epsilon = 0.0001;

        if (bubblestratPokemons == null) {
            loadBubblestratPokemon();
        }

        if (Math.abs(level - 2) < epsilon) { // all bubblestrat compatible Pokémon are level 2 or lower
            return false;
        }

        for (BubblestratPokemon bubblestratPokemon : bubblestratPokemons) {
            if (bubblestratPokemon.getPokeNr() == pokemonId && Math.abs(bubblestratPokemon.getMaxLevel() - level) < epsilon  && (bubblestratPokemon.getMoveNr() == moveID)) {
                return true;
            }
        }

        return false;
    }

}
