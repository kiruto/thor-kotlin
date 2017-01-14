package com.exyui.yell.core.database

/**
 * Created by yuriel on 1/14/17.
 */

const val SQL_VERSION = 1

const val CREATE_COMMENT = "CREATE TABLE IF NOT EXISTS comments (tid REFERENCES threads(id), id INTEGER PRIMARY KEY, parent INTEGER, created FLOAT NOT NULL, modified FLOAT, mode INTEGER, remote_addr VARCHAR, text VARCHAR, author VARCHAR, email VARCHAR, website VARCHAR, likes INTEGER DEFAULT 0, dislikes INTEGER DEFAULT 0, voters BLOB NOT NULL);"
const val CREATE_THREAD = "CREATE TABLE IF NOT EXISTS threads (id INTEGER PRIMARY KEY, uri VARCHAR(256) UNIQUE, title VARCHAR(256));"
const val CREATE_PREFERENCE = "CREATE TABLE IF NOT EXISTS preferences (key VARCHAR PRIMARY KEY, value VARCHAR);"
