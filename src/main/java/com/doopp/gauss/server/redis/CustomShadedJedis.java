package com.doopp.gauss.server.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.ArrayList;
import java.util.List;

public class CustomShadedJedis {

    private final Logger logger = LoggerFactory.getLogger(CustomShadedJedis.class);

    private ShardedJedisPool shardedJedisPool;

    //  private ShardedJedis shardedJedis;

    private final JdkSerializationRedisSerializer redisSerializer = new JdkSerializationRedisSerializer();

    public void setex(String key, int seconds, String value) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            shardedJedis.setex(key, seconds, value);
        }
    }

    public void set(String key, String value) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            shardedJedis.set(key, value);
        }
    }

    public String get(String key) {
        String value;
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            value = shardedJedis.get(key);
        }
        return value;
    }

    public void del(String... keys) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            for (String key : keys) {
                shardedJedis.del(key);
            }
        }
    }

    public void setex(byte[] key, int seconds, Object object) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            byte[] _object = redisSerializer.serialize(object);
            shardedJedis.setex(key, seconds, _object);
        }
    }

    public <T> void set(byte[] key, T object) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            byte[] _object = redisSerializer.serialize(object);
            shardedJedis.set(key, _object);
        }
    }

    public <T> T get(byte[] key, Class<T> clazz) {
        byte[] _object;
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            _object = shardedJedis.get(key);
        }
        if (_object == null) {
            return null;
        }
        return clazz.cast(redisSerializer.deserialize(_object));
    }

    public void del(byte[]... keys) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            for (byte[] key : keys) {
                shardedJedis.del(key);
            }
        }
    }

    public <T> void setList(String key, List<T> list) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            shardedJedis.set(key.getBytes(), redisSerializer.serialize(list));
        }
    }

    public <T> List<T> getList(String key, Class<T> clazz) {
        byte[] in;
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            in = shardedJedis.get(key.getBytes());
        }
        // return (List<T>) redisSerializer.deserialize(in);
        List listDes = (List) redisSerializer.deserialize(in);
        List<T> listRst = new ArrayList<>();
        for (Object listItem : listDes) {
            listRst.add(clazz.cast(listItem));
        }
        return listRst;
    }

    public void setShardedJedisPool(ShardedJedisPool shardedJedisPool) {
        this.shardedJedisPool = shardedJedisPool;
        // this.shardedJedis = shardedJedisPool.getResource();
    }

    public ShardedJedisPool getShardedJedisPool() {
        return this.shardedJedisPool;
    }
}
