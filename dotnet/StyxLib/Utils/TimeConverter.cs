// TimeConverter.cs - Exposes methods for converting between time formats
// Author:  Vladimir Shcryabets <vshcryabets@2vsoft.com>
using System;

namespace StyxLib.Utils
{
    /// <summary>
    /// Time converting routines
    /// </summary>
    public class TimeConverter
    {
        protected static readonly ulong epochStart = (ulong)(new DateTime(1970, 1, 1)).Ticks; // UTP offset
        protected static DateTime epochStartDT = new DateTime(1970, 1, 1); // UTP first date

        /// <summary>
        /// This function converts DateTime to UTP
        /// </summary>
        /// <param name="dt"></param>
        /// <returns></returns>
        public static ulong ConvertToUTP(DateTime dt)
        {
            //converting from 100-nanosecons intervals to 1-second intervals
            return ((ulong)dt.Ticks - epochStart) / 10000000;
        }

        /// <summary>
        /// This function converts from UTP to DateTime
        /// </summary>
        /// <param name="dt"></param>
        /// <returns></returns>
        public static DateTime ConvertFromUTP(ulong tp)
        {
            return epochStartDT.Add(new TimeSpan((long)(tp * 10000000)));
        }

        /// <summary>
        /// Formatting time string
        /// </summary>
        /// <param name="hour"></param>
        /// <param name="minute"></param>
        /// <param name="seconds"></param>
        /// <returns></returns>
        public static string TimeToStr(int hour, int minute, int seconds)
        {
            string res = "";
            if (hour < 10)
                res += "0" + hour;
            else
                res += hour;
            res += ":";
            if (minute < 10)
                res += "0" + minute;
            else
                res += minute;
            res += ":";
            if (seconds < 10)
                res += "0" + seconds;
            else
                res += seconds;
            return res;
        }

        /// <summary>
        /// Parsing time string
        /// </summary>
        /// <param name="time"></param>
        /// <returns></returns>
        public static TimeSpan StrToTime(string time)
        {
            string[] data = time.Split(':');
            if (data.Length != 3) throw new Exception("Wrong time format(1)");
            int hour = 0, min = 0, sec = 0;
            hour = Int32.Parse(data[0]);
            min = Int32.Parse(data[1]);
            sec = Int32.Parse(data[2]);
            return new TimeSpan(hour, min, sec); ;
        }
    }

}
