package com.masterproject.fittam.googleApis;


/**
 *
 * Thus class hold object weeklyData that used by History API to store data and
 * pass to Recycler (History adpter) and barfragment
 */

public class WeeklyData {

        private String day;
        private String value;
        private String calories;
        private String date;


        public WeeklyData(String day, String date, String value, String calories) {
            this.day = day;
            this.date = date;
            this.value = value;
            this.calories = calories;



        }

        public String getDay() {
            return day;
        }

        public String getDate() {
            return date;
        }

        public String getValue() {
            return value;
        }

        public String getCalories() {
            return calories;
        }





}

