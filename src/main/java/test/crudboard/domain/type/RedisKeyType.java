package test.crudboard.domain.type;



public enum RedisKeyType {
    HOT_POST_DATA("hot_post:%d"),
    HOT_POST_LIST("hot_post_set");

    private final String key;
    RedisKeyType(String key){
        this.key = key;
    }

    public String formatKey(Long id) {
        return String.format(key, id);
    }
}
