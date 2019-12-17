package com.example.iotapp;

import java.util.concurrent.locks.ReentrantLock;


public class AppState
{
    private String shellExchange;

    private ReentrantLock lock = new ReentrantLock();

    public AppState(String shellExchange) {
        this.shellExchange = shellExchange;
    }

    public void setValue(String addition)
    {
        lock.lock();
        try
        {
            shellExchange = shellExchange + addition;
        }
        finally
        {
            lock.unlock();
        }
    }

    public String getShellExchangeString()
    {
        lock.lock();
        try
        {
            return shellExchange;
        }
        finally
        {
            lock.unlock();
        }
    }
//rest of the class body goes here
}
