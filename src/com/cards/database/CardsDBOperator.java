package com.cards.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: andrey.moskvin
 * Date: 10/10/12
 * Time: 3:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class CardsDBOperator extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "TCGCardsDatabase";
    private static final String TABLE_NAME = "cards";
    private static final String SELECT_ALL_QUERY = "SELECT  * FROM " + TABLE_NAME;

    private static final String KEY_ID = "id";
    private GenerateDatabaseTask mAddTask;
    private Callback mCallback;

    public CardsDBOperator(Context context, Callback callback) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mAddTask = new GenerateDatabaseTask();
        mCallback = callback;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_CARDS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY," +
                Card.keyTypeTable() + ")";
        sqLiteDatabase.execSQL(CREATE_CARDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);

        onCreate(sqLiteDatabase);
    }

    public void addCards(List<Card> cards){
        if (cards != null) {
            mAddTask.execute(cards, null, null);
        }
    }

    public boolean databaseIsEmpty(){
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(SELECT_ALL_QUERY, null);

        boolean isEmpty = cursor.getCount() > 0;

        ArrayList<String> keys = new ArrayList<String>();
        Collections.addAll(keys, cursor.getColumnNames());
        Card.setAttributeKeys(keys);

        cursor.close();
        db.close();

        return isEmpty;
    }

    public ArrayList<Card> getAllCards(){
        ArrayList<Card> cardArrayList = new ArrayList<Card>();

        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(SELECT_ALL_QUERY, null);

        if (cursor.moveToFirst()) {
            do{
               int columns = cursor.getColumnCount();
               String[] fields = new String[columns];
               for (int i=0; i < columns; i++) {
                    fields[i] = cursor.getString(i);
               }
               Card card = new Card(fields);
               cardArrayList.add(card);
            }while (cursor.moveToNext());
        }
        return cardArrayList;
    }

    private class GenerateDatabaseTask extends AsyncTask<List<Card>,Void,Void> {

        private int mCardsCount;
        @Override
        protected Void doInBackground(List<Card>... lists) {
            List<Card> cards = lists[0];

            SQLiteDatabase database = getWritableDatabase();

            for (Card card : cards) {
                ContentValues values = card.toContentValues();
                database.insert(TABLE_NAME, null, values);
            }

            mCardsCount = cards.size();
            database.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(mCallback != null) {
                mCallback.databaseGenerationFinished(true,mCardsCount);
            }
            super.onPostExecute(aVoid);
        }
    }
    public static interface Callback{
        void databaseGenerationFinished(Boolean success, int itemsAdded);
    }
}