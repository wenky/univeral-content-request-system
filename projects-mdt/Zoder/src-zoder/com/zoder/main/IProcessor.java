package com.zoder.main;

import java.util.Map;

import com.zoder.util.Context;

public interface IProcessor 
{
    public void process(Map script,Context context) throws Exception;
}
