package psp.taxcredits.model {
    import mx.collections.ArrayCollection;

    public class ModelUtils {
        public function ModelUtils() {
        }

        private static const millisecondsPerDay:int = 1000 * 60 * 60 * 24;


        //returns days(date2 - date1)
        public static function dayDifference(date1:Date, date2:Date):int {
            //copy and strip out time
            var date2Copy:Date = new Date(date2.fullYear, date2.month, date2.date);
            var date1Copy:Date = new Date(date1.fullYear, date1.month, date1.date);

            var tempDate:Date = new Date(date2.getTime() - date1.getTime());
            return Math.round((tempDate.time / millisecondsPerDay));
        }

        public static function addDays(date1:Date,  days:int):Date {
            var newDate:Date = new Date();
            newDate.setTime(date1.getTime() + millisecondsPerDay * days);
            //strip time
            return new Date(newDate.fullYear, newDate.month, newDate.date);
        }

        public static const states:ArrayCollection = new ArrayCollection(["",
            "AK","AL","AR","AZ","CA","CO","CT","DC","DE","FL","GA","HI","IA","ID","IL","IN","KS","KY","LA","MA",
            "MD","ME","MI","MN","MO","MS","MT","NC","ND","NE","NH","NJ","NM","NV","NY","OH","OK","OR","PA","RI",
            "SC","SD","TN","TX","UT","VA","VT","WA","WI","WV","WY"
        ]);

        //return string representation (MMDDYYYY) (one-index month) of date so no AMF time zone weirdness
        public static function dateToString(date:Date):String {
            return pad2((date.getMonth() +1).toString()) + pad2(date.getDate().toString()) + date.getFullYear();
        }

        public static function pad2(string:String):String {
            if (string == null || string.length == 0) {
                return "00";
            } else if (string.length == 1) {
                return "0" + string;
            } else {
                return string;
            }
        }

        
    }
}                                     