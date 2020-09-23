package bus;

import hbase.client.HBaseClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DataCSVToHBaseLoader {

    private static final SimpleDateFormat datetimeFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static void loadDataSet(String filename, HBaseClient client, int maxNumberOfRows) throws IOException {

        /* Create HBase Table */
        boolean created = createTable(client);

        /* Avoid re-importing data */
        if (!created){
            System.out.println("Table already existing. Data import skipped");
            return;
        }

        /* Check if file exists */
        File f = new File(filename);
        if(!f.exists() || f.isDirectory()){
            throw new IOException("Invalid data set file: " + filename);
        }

        /* Copy file to HBase */
        copyDataSet(filename, client, maxNumberOfRows);
    }

    private static boolean createTable(HBaseClient hbc){
        if (!hbc.exists(HTableDescriptor.TABLE_NAME)){
            System.out.println("Creating table " + HTableDescriptor.TABLE_NAME + "...");
            hbc.createTable(HTableDescriptor.TABLE_NAME, HTableDescriptor.COLUMN_FAMILY);
            return true;
        }
        return false;
    }

    private static void copyDataSet(String filename, HBaseClient client, int maxNumberOfRows){
        int addedRows = 0;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {

                System.out.println(line);
                line = reader.readLine();
                insertLineToHBase(client, line);

                // Exit from loop
                if (addedRows > maxNumberOfRows) {
                    break;
                }
                addedRows++;
            }
            System.out.println("Imported " + addedRows + " lines.");
            reader.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static void insertLineToHBase(HBaseClient hbc, String line) throws ParseException {

        String[] r = line.split(",");

        long ts = fromTimeToTimeStamp(r[HTableDescriptor.COLUMNS.RECORDED_AT_TIME.ordinal()]);
        String publishedLine = r[HTableDescriptor.COLUMNS.PUBLISHED_LINE_NAME.ordinal()];
        String vehicleRef = r[HTableDescriptor.COLUMNS.VEHICLE_REF.ordinal()];
        String rowKey = generateRowKey("MTA", publishedLine, ts, vehicleRef);

        String[] cols = {
            HTableDescriptor.COLUMN_FAMILY, HTableDescriptor.RECORDED_AT_TIME,  r[HTableDescriptor.COLUMNS.RECORDED_AT_TIME.ordinal()],
            HTableDescriptor.COLUMN_FAMILY, HTableDescriptor.DIRECTION_REF,     r[HTableDescriptor.COLUMNS.DIRECTION_REF.ordinal()],
            HTableDescriptor.COLUMN_FAMILY, HTableDescriptor.PUBLISHED_LINE_NAME, r[HTableDescriptor.COLUMNS.PUBLISHED_LINE_NAME.ordinal()],
            HTableDescriptor.COLUMN_FAMILY, HTableDescriptor.ORIGIN_NAME,       r[HTableDescriptor.COLUMNS.ORIGIN_NAME.ordinal()],
            HTableDescriptor.COLUMN_FAMILY, HTableDescriptor.ORIGIN_LAT,        r[HTableDescriptor.COLUMNS.ORIGIN_LAT.ordinal()],
            HTableDescriptor.COLUMN_FAMILY, HTableDescriptor.ORIGIN_LONG,       r[HTableDescriptor.COLUMNS.ORIGIN_LONG.ordinal()],
            HTableDescriptor.COLUMN_FAMILY, HTableDescriptor.DESTINATION_NAME,  r[HTableDescriptor.COLUMNS.DESTINATION_NAME.ordinal()],
            HTableDescriptor.COLUMN_FAMILY, HTableDescriptor.DESTINATION_LAT,   r[HTableDescriptor.COLUMNS.DESTINATION_LAT.ordinal()],
            HTableDescriptor.COLUMN_FAMILY, HTableDescriptor.DESTINATION_LONG,  r[HTableDescriptor.COLUMNS.DESTINATION_LONG.ordinal()],
            HTableDescriptor.COLUMN_FAMILY, HTableDescriptor.VEHICLE_REF,       r[HTableDescriptor.COLUMNS.VEHICLE_REF.ordinal()],
            HTableDescriptor.COLUMN_FAMILY, HTableDescriptor.VEHICLE_LOCATION_LATITUDE,  r[HTableDescriptor.COLUMNS.VEHICLE_LOCATION_LATITUDE.ordinal()],
            HTableDescriptor.COLUMN_FAMILY, HTableDescriptor.VEHICLE_LOCATION_LONGITUDE, r[HTableDescriptor.COLUMNS.VEHICLE_LOCATION_LONGITUDE.ordinal()],
            HTableDescriptor.COLUMN_FAMILY, HTableDescriptor.NEXT_STOP_POINT_NAME,     r[HTableDescriptor.COLUMNS.NEXT_STOP_POINT_NAME.ordinal()],
            HTableDescriptor.COLUMN_FAMILY, HTableDescriptor.ARRIVAL_PROXIMITY_TEXT,   r[HTableDescriptor.COLUMNS.ARRIVAL_PROXIMITY_TEXT.ordinal()],
            HTableDescriptor.COLUMN_FAMILY, HTableDescriptor.DISTANCE_FROM_STOP,       r[HTableDescriptor.COLUMNS.DISTANCE_FROM_STOP.ordinal()],
            HTableDescriptor.COLUMN_FAMILY, HTableDescriptor.EXPECTED_ARRIVAL_TIME,    r[HTableDescriptor.COLUMNS.EXPECTED_ARRIVAL_TIME.ordinal()],
            HTableDescriptor.COLUMN_FAMILY, HTableDescriptor.SCHEDULED_ARRIVAL_TIME,   r[HTableDescriptor.COLUMNS.SCHEDULED_ARRIVAL_TIME.ordinal()],
        };

        hbc.put(HTableDescriptor.TABLE_NAME, rowKey, cols);

    }
    private static long fromTimeToTimeStamp(String date) throws ParseException {

        long ts = datetimeFormatter.parse(date).getTime();
        return ts - (ts % (1000 * 60 * 60));
    }

    private static String generateRowKey(
            String mta, String publishedLineName,
            long recordingTimestamp, String vehicleRef) {
        String SEP = "/";
        return mta + SEP + publishedLineName + SEP + recordingTimestamp + SEP + vehicleRef;
    }
}
