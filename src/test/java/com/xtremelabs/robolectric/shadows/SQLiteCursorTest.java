package com.xtremelabs.robolectric.shadows;


import android.database.sqlite.SQLiteCursor;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class SQLiteCursorTest {

    private Connection connection;
    private ResultSet resultSet;
    private SQLiteCursor cursor;

    @Before
    public void setUp() throws Exception {
        Class.forName("org.h2.Driver").newInstance();
        connection = DriverManager.getConnection("jdbc:h2:mem:");

        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE table_name(" +
                "id INT PRIMARY KEY, name VARCHAR(255), long_value BIGINT," +
                "float_value REAL, double_value DOUBLE, blob_value BINARY );");

        addPeople();
        setupCursor();
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testGetColumnNames() throws Exception {
        String[] columnNames = cursor.getColumnNames();

        assertColumnNames(columnNames);
    }

    @Test
    public void testGetColumnNamesEmpty() throws Exception {
        setupEmptyResult();
        String[] columnNames = cursor.getColumnNames();

        // Column names are present even with an empty result.
        assertThat(columnNames, notNullValue());
        assertColumnNames(columnNames);
    }

    @Test
    public void testGetColumnIndex() throws Exception {
        assertThat(cursor.getColumnIndex("id"), equalTo(0));
        assertThat(cursor.getColumnIndex("name"), equalTo(1));
    }

    @Test
    public void testGetColumnIndexNotFound() throws Exception {
        assertThat(cursor.getColumnIndex("Fred"), equalTo(-1));
    }

    @Test
    public void testGetColumnIndexEmpty() throws Exception {
        setupEmptyResult();

        assertThat(cursor.getColumnIndex("id"), equalTo(0));
        assertThat(cursor.getColumnIndex("name"), equalTo(1));
    }

    @Test
    public void testGetColumnIndexOrThrow() throws Exception {
        assertThat(cursor.getColumnIndexOrThrow("id"), equalTo(0));
        assertThat(cursor.getColumnIndexOrThrow("name"), equalTo(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetColumnIndexOrThrowNotFound() throws Exception {
        cursor.getColumnIndexOrThrow("Fred");
    }

    @Test
    public void testGetColumnIndexOrThrowEmpty() throws Exception {
        setupEmptyResult();

        assertThat(cursor.getColumnIndexOrThrow("name"), equalTo(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetColumnIndexOrThrowNotFoundEmpty() throws Exception {
        setupEmptyResult();

        cursor.getColumnIndexOrThrow("Fred");
    }

    @Test
    public void testMoveToFirst() throws Exception {
        assertThat(cursor.moveToFirst(), equalTo(true));
        assertThat(cursor.getInt(0), equalTo(1234));
        assertThat(cursor.getString(1), equalTo("Chuck"));
    }

    @Test
    public void testMoveToFirstEmpty() throws Exception {
        setupEmptyResult();

        assertThat(cursor.moveToFirst(), equalTo(false));
    }

    @Test
    public void testMoveToNext() throws Exception {
        cursor.moveToFirst();

        assertThat(cursor.moveToNext(), equalTo(true));
        assertThat(cursor.getInt(0), equalTo(1235));
        assertThat(cursor.getString(1), equalTo("Julie"));
    }

    @Test
    public void testMoveToNextPastEnd() throws Exception {
        cursor.moveToFirst();

        cursor.moveToNext();
        cursor.moveToNext();

        assertThat(cursor.moveToNext(), equalTo(false));
    }

    @Test
    public void testMoveToNextEmpty() throws Exception {
        setupEmptyResult();

        cursor.moveToFirst();
        assertThat(cursor.moveToNext(), equalTo(false));
    }

    @Test
    public void testGetPosition() throws Exception {
        cursor.moveToFirst();
        assertThat(cursor.getPosition(), equalTo(0));

        cursor.moveToNext();
        assertThat(cursor.getPosition(), equalTo(1));
    }

    @Test
    public void testGetBlob() throws Exception {
        String sql = "UPDATE table_name set blob_value=? where id=1234";
        byte[] byteData = sql.getBytes();

        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setObject(1, byteData);
        statement.executeUpdate();

        setupCursor();
        cursor.moveToFirst();

        byte[] retrievedByteData = cursor.getBlob(5);
        assertThat(byteData.length, equalTo(retrievedByteData.length));

        for (int i = 0; i < byteData.length; i++) {
            assertThat(byteData[i], equalTo(retrievedByteData[i]));
        }
    }

    @Test
    public void testGetString() throws Exception {
        cursor.moveToFirst();

        String[] data = {"Chuck", "Julie", "Chris"};

        for (String aData : data) {
            assertThat(cursor.getString(1), equalTo(aData));
            cursor.moveToNext();
        }
    }

    @Test
    public void testGetInt() throws Exception {
        cursor.moveToFirst();

        int[] data = {1234, 1235, 1236};

        for (int aData : data) {
            assertThat(cursor.getInt(0), equalTo(aData));
            cursor.moveToNext();
        }
    }

    @Test
    public void testGetLong() throws Exception {
        cursor.moveToFirst();

        assertThat(cursor.getLong(2), equalTo(3463L));
    }

    @Test
    public void testGetFloat() throws Exception {
        cursor.moveToFirst();

        assertThat(cursor.getFloat(3), equalTo((float) 1.5));
    }

    @Test
    public void testGetDouble() throws Exception {
        cursor.moveToFirst();

        assertThat(cursor.getDouble(4), equalTo(3.14159));
    }

    @Test
    public void testClose() throws Exception {
        assertThat(cursor.isClosed(), equalTo(false));
        cursor.close();
        assertThat(cursor.isClosed(), equalTo(true));
    }

    @Test
    public void testIsNullWhenNull() throws Exception {
        cursor.moveToFirst();
        assertThat(cursor.moveToNext(), equalTo(true));

        assertThat(cursor.isNull(cursor.getColumnIndex("id")), equalTo(false));
        assertThat(cursor.isNull(cursor.getColumnIndex("name")), equalTo(false));

        assertThat(cursor.isNull(cursor.getColumnIndex("long_value")), equalTo(true));
        assertThat(cursor.isNull(cursor.getColumnIndex("float_value")), equalTo(true));
        assertThat(cursor.isNull(cursor.getColumnIndex("double_value")), equalTo(true));
    }

    @Test
    public void testIsNullWhenNotNull() throws Exception {
        cursor.moveToFirst();

        for (int i = 0; i < 5; i++) {
            assertThat(cursor.isNull(i), equalTo(false));
        }
    }

    @Test
    public void testIsNullWhenIndexOutOfBounds() throws Exception {
        cursor.moveToFirst();

        // column index 5 is out-of-bounds
        assertThat(cursor.isNull(5), equalTo(true));
    }

    private void addPeople() throws Exception {
        String[] inserts = {
                "INSERT INTO table_name (id, name, long_value, float_value, double_value) VALUES(1234, 'Chuck', 3463, 1.5, 3.14159);",
                "INSERT INTO table_name (id, name) VALUES(1235, 'Julie');",
                "INSERT INTO table_name (id, name) VALUES(1236, 'Chris');"
        };

        for (String insert : inserts) {
            connection.createStatement().executeUpdate(insert);
        }
    }

    private void setupCursor() throws Exception {
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        resultSet = statement.executeQuery("SELECT * FROM table_name;");
        cursor = new SQLiteCursor(null, null, null, null);
        Robolectric.shadowOf(cursor).setResultSet(resultSet);
    }

    private void setupEmptyResult() throws Exception {
        Statement statement = connection.createStatement();
        statement.executeUpdate("DELETE FROM table_name;");

        setupCursor();
    }

    private void assertColumnNames(String[] columnNames) {
        assertThat(columnNames.length, equalTo(6));
        assertThat(columnNames[0], equalTo("ID"));
        assertThat(columnNames[1], equalTo("NAME"));
        assertThat(columnNames[2], equalTo("LONG_VALUE"));
        assertThat(columnNames[3], equalTo("FLOAT_VALUE"));
        assertThat(columnNames[4], equalTo("DOUBLE_VALUE"));
        assertThat(columnNames[5], equalTo("BLOB_VALUE"));
    }

}
