package test.crudboard.type;



public enum RedisKeyType {
    POST_DATA("post:%d:data"),
    POST_STATS("post:%d:stats"),
    POST_ALL("post:%d:*");

    private final String key;
    RedisKeyType(String key){
        this.key = key;
    }

    public String formatKey(Long id) {
        return String.format(key, id);
    }
}
