package com.exyui.yell.core.database

/**
 * Created by yuriel on 1/14/17.
 */
const val CREATE_COMMENT = "CREATE TABLE IF NOT EXISTS comments (tid REFERENCES threads(id), id INTEGER PRIMARY KEY, parent INTEGER, created FLOAT NOT NULL, modified FLOAT, mode INTEGER, remote_addr VARCHAR, text VARCHAR, author VARCHAR, email VARCHAR, website VARCHAR, likes INTEGER DEFAULT 0, dislikes INTEGER DEFAULT 0, voters BLOB NOT NULL);"
