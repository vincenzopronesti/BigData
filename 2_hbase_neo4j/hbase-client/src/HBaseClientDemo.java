import bus.HTableDescriptor;
import com.google.protobuf.ServiceException;
import hbase.client.HBaseClient;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.Map;

public class HBaseClientDemo {

    private static byte[] b(String s){
        return Bytes.toBytes(s);
    }

    public static void tableManagementOperations(HBaseClient hbc) throws IOException, ServiceException {

        /* **********************************************************
         *  Table Management: Create, Alter, Describe, Delete
         * ********************************************************** */
        //  Create
        if (!hbc.exists("products")){
            System.out.println("Creating table...");
            hbc.createTable("products",
                    "fam1", "fam2", "fam3");
        }


        // List tables
        System.out.println("Listing table...");
        System.out.println(hbc.listTables());


        //  Alter
        System.out.println("Altering tables...");
        System.out.println(hbc.describeTable("products"));
        hbc.alterColumnFamily(HBaseClient.ALTER_COLUMN_FAMILY.DELETE, "products","fam3");
        System.out.println(hbc.describeTable("products"));
        hbc.alterColumnFamily(HBaseClient.ALTER_COLUMN_FAMILY.ADD, "products","fam4");
        System.out.println(hbc.describeTable("products"));


        //  Scan table
        System.out.println("Scanning table...");
        hbc.scanTable("products", null, null);


        //  Drop table
        System.out.println("Deleting table...");
        hbc.dropTable("products");

    }


    public static void simpleDataManipulationOperations(HBaseClient hbc) throws IOException, ServiceException {


        /* **********************************************************
         *  Data Management: Put, Get, Delete, Scan, Truncate
         * ********************************************************** */

        /* Create */
        System.out.println("\n******************************************************** \n");
        if (!hbc.exists("products")){
            System.out.println("Creating table...");
            hbc.createTable("products", "fam1", "fam2", "fam3");
        }
        System.out.println("\n******************************************************** \n");


        /* Put */
        System.out.println("\n ------------------\n");
        System.out.println("Adding value: row1:fam1:col1=val1");
        hbc.put("products", "row1", "fam1", "col1", "val1");
        System.out.println("Adding value: row1:fam1:col2=val2");
        hbc.put("products", "row1", "fam1", "col2", "val2");
        System.out.println("Adding value: row1:fam2:col2=val2");
        hbc.put("products", "row1", "fam2", "col2", "val2");
        System.out.println("Adding value: row1:fam2:col2=val3");
        hbc.put("products", "row1", "fam2", "col2", "val3");


        /* Get */
        String v1 = hbc.get("products",
                "row1", "fam1", "col1");
        String v2 = hbc.get("products",
                "row1", "fam1", "col2");
        String v3 = hbc.get("products",
                "row1", "fam2", "col2");
        System.out.println("Retrieving values : " + v1 + "; " + v2 + "; " + v3);
        System.out.println("\n ------------------\n");


        /* Scan table */
        System.out.println("Scanning table...");
        hbc.scanTable("products", null, null);
        System.out.println("\n ------------------\n");


        /* Update a row (the row key is unique) */
        System.out.println("Updating value: row1:fam1:col1=val1 to row1:fam1:col1=val3");
        hbc.put("products", "row1", "fam1", "col1", "val3");
        v1 = hbc.get("products", "row1", "fam1", "col1");
        v2 = hbc.get("products", "row1", "fam1", "col2");
        System.out.println("Retrieving values : " + v1 + "; " + v2);
        System.out.println("\n ------------------\n");


        /* Delete a column (with a previous value stored within) */
        System.out.println("Deleting value: row1:fam1:col1");
        v1 = hbc.get("products", "row1", "fam1", "col1");
        System.out.println("Retrieving value row1:fam1:col1 (pre-delete): " + v1);

        hbc.delete("products", "row1", "fam1", "col1");
        v1 = hbc.get("products", "row1", "fam1", "col1");
        System.out.println("Retrieving value row1:fam1:col1 (post-1st-delete): " + v1);

        hbc.delete("products", "row1", "fam1", "col1");
        v1 = hbc.get("products", "row1", "fam1", "col1");
        System.out.println("Retrieving value row1:fam1:col1 (post-2nd-delete): " + v1);
        System.out.println("\n ------------------\n");


        /* Scanning table */
        System.out.println("Scanning table...");
        hbc.scanTable("products", null, null);


        /* Truncate */
        System.out.println("Truncating data... ");
        hbc.truncateTable("products", true);
        System.out.println("\n ------------------\n");


        /* Scanning table */
        System.out.println("Scanning table...");
        hbc.scanTable("products", null, null);

    }

    public static void otherDataManipulationOperations(HBaseClient hbc) throws IOException, ServiceException {

        /* Create */
        System.out.println("\n******************************************************** \n");
        if (!hbc.exists("products")){
            System.out.println("Creating table...");
            hbc.createTable("products", "fam1", "fam2", "fam3");
        }
        System.out.println("\n******************************************************** \n");

        /* **********************************************************
         *  Data Management: Special Cases of Put, Delete
         * ********************************************************** */

        /* Try to insert a row for a not existing column family */
        System.out.println("Insert a key with a not existing column family");
        boolean res = hbc.put("products",
                "row2", "fam100", "col1", "val1");
        System.out.println(" result: " + res);
        System.out.println("\n ------------------\n");


        /* Delete: different columns, same column family */
        System.out.println(" # Inserting row2:fam1:col1");
        hbc.put("products", "row2", "fam1", "col1", "val1");
        System.out.println(" # Inserting row2:fam1:col2 ");
        hbc.put("products", "row2", "fam1", "col2", "val2");

        String v1 = hbc.get("products", "row2", "fam1", "col1");
        String v2 = hbc.get("products", "row2", "fam1", "col2");
        System.out.println("Retrieving values (pre-delete of col1): " + v1 + "; " + v2);

        System.out.println("Deleting data of different columns, but same column family... ");
        hbc.delete("products", "row2", "fam1", "col1");
        v1 = hbc.get("products", "row2", "fam1", "col1");
        v2 = hbc.get("products", "row2", "fam1", "col2");
        System.out.println("Retrieving values (post-delete of col1): " + v1 + "; " + v2);

        System.out.println("\n ------------------\n");


        /* Cleaning up all the data, before showing the next example */
        hbc.truncateTable("products", false);

        /* Delete: column family */
        System.out.println(" # Inserting row2:fam1:col2 = val2 (same family of existing row)");
        hbc.put("products", "row2", "fam1", "col2", "val2");
        System.out.println(" # Inserting row2:fam2:col3 = val3 (same family of existing row)");
        hbc.put("products", "row2", "fam2", "col3", "val3");
        System.out.println();

        System.out.println("Deleting a column family for a data... ");
        hbc.delete("products", "row2", "fam1", null);
        v1 = hbc.get("products", "row2", "fam1", "col1");
        v2 = hbc.get("products", "row2", "fam1", "col2");
        String v3 = hbc.get("products", "row2", "fam2", "col3");
        System.out.println("Retrieving values : " + v1 + "; " + v2 + "; " + v3);
        System.out.println("\n ------------------\n");

        // note that we can insert: v1 = row2:fam1:col1
        res = hbc.put("products",
                "row2", "fam1", "col1", "val1");
        System.out.println(" result: " + res);
        v1 = hbc.get("products", "row2", "fam1", "col1");
        System.out.println("Retrieving value : " + v1);
        System.out.println("\n ------------------\n");


        /* Delete: entire row key */
        System.out.println("Deleting the whole row (row2)... ");
        hbc.delete("products",
                "row2", null, null);
        v1 = hbc.get("products", "row2", "fam1", "col1");
        v2 = hbc.get("products", "row2", "fam1", "col2");
        v3 = hbc.get("products", "row2", "fam2", "col3");
        System.out.println("Retrieving values : " + v1 + "; " + v2 + "; " + v3);

    }



    public static void getAllVersions(HBaseClient hbc) throws IOException, ServiceException {
        /* Create */
        System.out.println("\n******************************************************** \n");
        if (!hbc.exists("products")){
            System.out.println("Creating table...");
            hbc.createTable("products", "fam1", "fam2", "fam3");
        }
        System.out.println("\n******************************************************** \n");


        /* Put */
        System.out.println("\n ------------------\n");
        System.out.println("Adding value: row1:fam1:col1=val1");
        hbc.put("products", "row1", "fam1", "col1", "val1");
        System.out.println("Adding value: row1:fam1:col1=val2");
        hbc.put("products", "row1", "fam1", "col1", "val2");

        /* Get All Versions */
        System.out.println("Retrieving all versions of row1:fam1:col1");
        Map<Long, String> values = hbc.getAllVersions("products", "row1", "fam1", "col1");

        for (long ts : values.keySet()){
            System.out.println(" - " + ts + ": " + values.get(ts));
        }


        // Deleting a value an trying again:
        System.out.println("Deleting value of row1:fam1:col1");
        hbc.delete("products", "row1", "fam1", "col1");
        values = hbc.getAllVersions("products", "row1", "fam1", "col1");
        System.out.println("Retrieving all versions of row1:fam1:col1");
        for (long ts : values.keySet()){
            System.out.println(" - " + ts + ": " + values.get(ts));
        }

        //  Drop table
        System.out.println("Deleting table...");
        hbc.dropTable("products");

    }


    public static void filter(HBaseClient hbc) throws IOException, ServiceException {


        /* **********************************************************
         *  Data Management: Filter
         * ********************************************************** */


        /* Create */
        System.out.println("\n******************************************************** \n");
        if (!hbc.exists("products")){
            System.out.println("Creating table...");
            hbc.createTable("products", "fam1", "fam2", "fam3");
        }
        System.out.println("\n******************************************************** \n");


        /* Put: different rows, same column family */
        System.out.println(" # Inserting row1:fam1:col1");
        hbc.put("products", "row1", "fam1", "col1", "test");
        System.out.println(" # Inserting row2:fam1:col1 ");
        hbc.put("products", "row2", "fam1", "col1", "val");

        String v1 = hbc.get("products", "row1", "fam1", "col1");
        String v2 = hbc.get("products", "row2", "fam1", "col1");
        System.out.println("Retrieving values: " + v1 + "; " + v2);



        Table table = hbc.getConnection().getTable(TableName.valueOf("products"));
        Scan scan = new Scan();

        // We add to the scan the column we are interested in and the column we want to filter on
        scan.addColumn(b("fam1"), b("col1"));


        // We specify the row prefix for starting the scan operation
        scan.withStartRow(b("row"))
                // we set the stop condition as follows: proceed until scanning all rows with specified prefix
                .setRowPrefixFilter(b("row"));


            SingleColumnValueFilter valueFilter =
                    new SingleColumnValueFilter(
                            b("fam1"),
                            b("col1"),
                            CompareFilter.CompareOp.NOT_EQUAL,
                            b("val"));


            scan.setFilter(valueFilter);


        ResultScanner scanner = table.getScanner(scan);


        // Emitting results
        int count = 0;
        for (Result r = scanner.next(); r != null; r = scanner.next()){

            byte[] test = r.getValue(b("fam1"), Bytes.toBytes("col1"));
            System.out.println(" - " + new String(r.getRow()) + ", Test result = " + new String(test));
            count++;
        }
        System.out.println(" Found: " + count + " entries");

        scanner.close();


        //  Drop table
        System.out.println("Deleting table...");
        hbc.dropTable("products");


    }


    public static void main(String[] args) throws IOException, ServiceException {

        HBaseClient hbc = new HBaseClient();

        int choice = 4;

        switch (choice) {

            case 1:

            /* **********************************************************
             *  Table Management: Create, Alter, Describe, Delete
             * ********************************************************** */
                tableManagementOperations(hbc);
                break;

            case 2:
                /* **********************************************************
                 *  Data Management: Put, Get, Delete, Scan, Truncate
                 * ********************************************************** */
                simpleDataManipulationOperations(hbc);
                break;

            case 3:

                /* Data manipulation, special cases */
                otherDataManipulationOperations(hbc);
                break;


            case 4:
            /* Get All Versions */
                getAllVersions(hbc);
                break;


            default:
            /* **********************************************************
             *  Data Management: Filter
            * ********************************************************** */
                filter(hbc);
                break;


        }

    }
}
