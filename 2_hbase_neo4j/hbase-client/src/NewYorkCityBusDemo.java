import bus.DataCSVToHBaseLoader;
import bus.HTableDescriptor;
import com.google.protobuf.ServiceException;
import hbase.client.HBaseClient;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.*;

public class NewYorkCityBusDemo {

    /* https://codelabs.developers.google.com/codelabs/cloud-bigtable-intro-java/index.html#2 */
    public static String DATASET = "data/mta_1710.reduced.csv";

    private static void firstQuery(HBaseClient hbc, String rowKey) throws IOException, ServiceException {

        // Instantiating HTable class
        Table hTable = hbc.getConnection().getTable(TableName.valueOf(HTableDescriptor.TABLE_NAME));
        Get g = new Get(b(rowKey))
                .addColumn(b(HTableDescriptor.COLUMN_FAMILY), b(HTableDescriptor.VEHICLE_LOCATION_LATITUDE))
                .addColumn(b(HTableDescriptor.COLUMN_FAMILY), b(HTableDescriptor.VEHICLE_LOCATION_LONGITUDE));
        Result result = hTable.get(g);

        String latitude = new String(result.getValue(b(HTableDescriptor.COLUMN_FAMILY), b(HTableDescriptor.VEHICLE_LOCATION_LATITUDE)));
        String longitude = new String(result.getValue(b(HTableDescriptor.COLUMN_FAMILY), b(HTableDescriptor.VEHICLE_LOCATION_LONGITUDE)));
        System.out.println("Retrieving value (last location) for key \'" + rowKey + "\': " + latitude + ", " + longitude);
        System.out.println();

    }

    private static void firstQuery_AllVersions(HBaseClient hbc, String rowKey) throws IOException, ServiceException {

        System.out.println("Retrieving all value for key \'"+rowKey + "\':");
        /* Get All Versions */
        Table hTable = hbc.getConnection().getTable(TableName.valueOf(HTableDescriptor.TABLE_NAME));
        Get g = new Get(b(rowKey))
                .setMaxVersions(Integer.MAX_VALUE)
                .addColumn(b(HTableDescriptor.COLUMN_FAMILY), b(HTableDescriptor.VEHICLE_LOCATION_LATITUDE))
                .addColumn(b(HTableDescriptor.COLUMN_FAMILY), b(HTableDescriptor.VEHICLE_LOCATION_LONGITUDE));

        // Reading the data
        Result result = hTable.get(g);
        List<Cell> cellsLat = result.getColumnCells(b(HTableDescriptor.COLUMN_FAMILY), b(HTableDescriptor.VEHICLE_LOCATION_LATITUDE));
        Map<Long, String> tsToLatitude = new HashMap<>();
        for (Cell la : cellsLat){
            tsToLatitude.put(la.getTimestamp(), new String(CellUtil.cloneValue(la)));
        }
        Map<Long, String> tsToLongitude = new HashMap<>();
        List<Cell> cellsLong = result.getColumnCells(b(HTableDescriptor.COLUMN_FAMILY), b(HTableDescriptor.VEHICLE_LOCATION_LONGITUDE));
        for (Cell lo : cellsLong){
            tsToLongitude.put(lo.getTimestamp(), new String(CellUtil.cloneValue(lo)));
        }

        // Reading the data
        for (long ts : tsToLatitude.keySet()){
            System.out.println(" - " + ts + ": " + tsToLatitude.get(ts) + ", " + tsToLongitude.get(ts));
        }

    }

    private static void secondQuery(HBaseClient hbc, String rowKeyPrefix, String destinationToMatch, boolean enableFilter) throws IOException, ServiceException {


        Table table = hbc.getConnection().getTable(TableName.valueOf(HTableDescriptor.TABLE_NAME));
        Scan scan = new Scan();

        // We add to the scan all columns we are interested in and all columns we want to filter on
        scan.addColumn(b(HTableDescriptor.COLUMN_FAMILY), b(HTableDescriptor.VEHICLE_LOCATION_LATITUDE))
                .addColumn(b(HTableDescriptor.COLUMN_FAMILY), b(HTableDescriptor.VEHICLE_LOCATION_LONGITUDE))
                .addColumn(b(HTableDescriptor.COLUMN_FAMILY), b(HTableDescriptor.DESTINATION_NAME))
                .setMaxVersions(1);

        // We specify the row prefix for starting the scan operation
        scan.withStartRow(b(rowKeyPrefix))
                // we set the stop condition as follows: proceed until scanning all rows with specified prefix
                .setRowPrefixFilter(b(rowKeyPrefix));

        if (enableFilter){
            SingleColumnValueFilter valueFilter =
                    new SingleColumnValueFilter(
                            b(HTableDescriptor.COLUMN_FAMILY),
                            b(HTableDescriptor.DESTINATION_NAME),
                            CompareFilter.CompareOp.EQUAL,
                            b(destinationToMatch));

            scan.setFilter(valueFilter);
        }

        ResultScanner scanner = table.getScanner(scan);


        // Emitting results
        int count = 0;
        for (Result r = scanner.next(); r != null; r = scanner.next()){
            byte[] latitude = r.getValue(b(HTableDescriptor.COLUMN_FAMILY), Bytes.toBytes(HTableDescriptor.VEHICLE_LOCATION_LATITUDE));
            byte[] longitude = r.getValue(b(HTableDescriptor.COLUMN_FAMILY), Bytes.toBytes(HTableDescriptor.VEHICLE_LOCATION_LONGITUDE));
            byte[] destination = r.getValue(b(HTableDescriptor.COLUMN_FAMILY), Bytes.toBytes(HTableDescriptor.DESTINATION_NAME));
            System.out.println(" - " + new String(r.getRow()) + ", " + new String(latitude) + ", " + new String(longitude) + "; Destination = " + new String(destination));
            count++;
        }
        System.out.println(" Found: " + count + " entries");

        scanner.close();

    }

    private static byte[] b(String s){
        return Bytes.toBytes(s);
    }


    public static void main(String[] args) throws IOException, ServiceException {

        HBaseClient hbc = new HBaseClient();

        DataCSVToHBaseLoader.loadDataSet(DATASET, hbc, 10000);

        /* Query1:
         *  The first query you'll perform is a simple row lookup.
         *  You'll get the data for a bus on the S78 line on October 1, 2017
         *  from 12:00 am to 1:00 am.
         *  A vehicle with id NYCT_8180 is on the bus line then. */
        System.out.println(" ********************************************************* ");
        System.out.println(" * QUERY 1  ");
        System.out.println(" ********************************************************* ");
        firstQuery(hbc, "MTA/S78/1506808800000/NYCT_8180");
        firstQuery_AllVersions(hbc, "MTA/S78/1506808800000/NYCT_8180");

        /* Query2:
        * Now, let's view *all* the data for the bus line for that hour.
        * The scan code looks pretty similar to the get code. You give the scanner a starting position and
        * then indicate you only want rows for the MTA/S78 bus line within the hour denoted by the
        * timestamp 1506823200000.
        * */
        System.out.println("\n ********************************************************* ");
        System.out.println(" * QUERY 2  ");
        System.out.println(" ********************************************************* ");
        secondQuery(hbc, "MTA/S78/1506823200000", "ST GEORGE FERRY via HYLAN", true);

    }
}
