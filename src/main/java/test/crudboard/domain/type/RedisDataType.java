package test.crudboard.domain.type;

public enum RedisDataType {
    POST_VIEW_COUNT("view:%d"),
    POST_LIKE_COUNT("like:%d");
    private final String key;
    RedisDataType(String key){
        this.key = key;
    }

    public String formatKey(Long id){
        return String.format(key,id/1000);
    }

}
