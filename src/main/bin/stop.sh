#!/usr/bin/env bash
ps aux|grep java|grep buaa-chain|grep -v grep|awk '{print $2}'|xargs kill -9