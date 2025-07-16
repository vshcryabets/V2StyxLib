package com.v2soft.styxlib.l5.v9p2000;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.TimeZone;

public class StringSerializerImplTests {
    StringSerializerImpl serializer = new StringSerializerImpl();

    @Test
    public void testSerializeQid() throws StyxException {
        Assertions.assertEquals("QID {type: 0, version: 0, path: 0}",
                serializer.serializeQid(StyxQID.EMPTY));
    }

    @Test
    public void testSerializeStat() throws StyxException {
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCalendar.set(2001, 05, 04, 12, 30, 23);
        utcCalendar.set(Calendar.MILLISECOND, 0);
        var date = utcCalendar.getTime();
        StyxStat stat = new StyxStat(
                0x10,
                0x20,
                StyxQID.EMPTY,
                0x123,
                date, date,
                123,
                "testName",
                "testUser",
                "testGroup",
                "testModUser"
        );

        Assertions.assertEquals("Stat 0x10,0x20,Qid=QID {type: 0, version: 0, path: 0},mode=0x123," +
                        "atime=2001-06-04T12:30:23Z,mtime=2001-06-04T12:30:23Z," +
                        "length=123,name=testName,user=testUser,group=testGroup,modUser=testModUser",
                serializer.serializeStat(stat));
    }
}
