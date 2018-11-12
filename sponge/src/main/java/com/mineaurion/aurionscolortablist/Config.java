package com.mineaurion.aurionscolortablist;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Config {
    public final static TypeToken<Config> type = TypeToken.of(Config.class);
    @Setting public String meta = "namecolor";
    @Setting public boolean prefix = false;
}
