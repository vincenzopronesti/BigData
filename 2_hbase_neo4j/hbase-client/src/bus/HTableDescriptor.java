package bus;

public class HTableDescriptor {

    public static String TABLE_NAME = "bus-data";
    public static String COLUMN_FAMILY = "cf";

    public enum COLUMNS {
        RECORDED_AT_TIME,
        DIRECTION_REF ,
        PUBLISHED_LINE_NAME ,
        ORIGIN_NAME ,
        ORIGIN_LAT ,
        ORIGIN_LONG ,
        DESTINATION_NAME ,
        DESTINATION_LAT ,
        DESTINATION_LONG ,
        VEHICLE_REF ,
        VEHICLE_LOCATION_LATITUDE ,
        VEHICLE_LOCATION_LONGITUDE ,
        NEXT_STOP_POINT_NAME ,
        ARRIVAL_PROXIMITY_TEXT ,
        DISTANCE_FROM_STOP ,
        EXPECTED_ARRIVAL_TIME ,
        SCHEDULED_ARRIVAL_TIME
    }
    public static String RECORDED_AT_TIME = "RecordedAtTime";
    public static String DIRECTION_REF = "DirectionRef";
    public static String PUBLISHED_LINE_NAME = "PublishedLineName";
    public static String ORIGIN_NAME = "OriginName";
    public static String ORIGIN_LAT = "OriginLat";
    public static String ORIGIN_LONG = "OriginLong";
    public static String DESTINATION_NAME = "DestinationName";
    public static String DESTINATION_LAT = "DestinationLat";
    public static String DESTINATION_LONG = "DestinationLong";
    public static String VEHICLE_REF = "VehicleRef";
    public static String VEHICLE_LOCATION_LATITUDE = "VehicleLocation.Latitude";
    public static String VEHICLE_LOCATION_LONGITUDE = "VehicleLocation.Longitude";
    public static String NEXT_STOP_POINT_NAME = "NextStopPointName";
    public static String ARRIVAL_PROXIMITY_TEXT = "ArrivalProximityText";
    public static String DISTANCE_FROM_STOP = "DistanceFromStop";
    public static String EXPECTED_ARRIVAL_TIME = "ExpectedArrivalTime";
    public static String SCHEDULED_ARRIVAL_TIME = "ScheduledArrivalTime";

}
