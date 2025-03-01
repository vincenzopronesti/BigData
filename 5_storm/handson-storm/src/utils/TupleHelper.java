package utils;

import org.apache.storm.Constants;
import org.apache.storm.tuple.Tuple;

public final class TupleHelper {

    public static boolean isTickTuple(Tuple tuple) {
        return tuple.getSourceComponent()
                .equals(Constants.SYSTEM_COMPONENT_ID)
                && tuple.getSourceStreamId()
                .equals(Constants.SYSTEM_TICK_STREAM_ID);
    }

}