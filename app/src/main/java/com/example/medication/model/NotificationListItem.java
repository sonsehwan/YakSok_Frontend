package com.example.medication.model;

public interface NotificationListItem {
    int TYPE_HEADER = 0;
    int TYPE_ITEM = 1;
    int getViewType();

    // 헤더 모델(아침, 점심, 저녁)
    public static class HeaderItem implements NotificationListItem {
        private String title;
        private String timeCategory;
        public HeaderItem(String title, String timeCategory){
            this.title = title;
            this.timeCategory = timeCategory;
        }
        public String getTitle(){ return title; }
        public String getTimeCategory(){ return timeCategory; }

        @Override
        public int getViewType() { return TYPE_HEADER; }
    }

    // 약 정보 모델
    public static class MedicationItem implements NotificationListItem {
        private NotificationYaksok data;
        public MedicationItem(NotificationYaksok data){ this.data = data; }
        public NotificationYaksok getData(){ return data; }

        @Override
        public int getViewType() { return TYPE_ITEM; }
    }

}
