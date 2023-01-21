<h1 align="center">
    <img src="./img/btree-logo.png" width="30%" height="30%">
    </br>
    B-Tree - Disk-based index file organization 
    </br>
</h1>

<p align="center">
    <a href="https://github.com/Silentsky0/btree-indexed-file-organization/blob/dev/LICENSE">
        <img src="https://img.shields.io/github/license/Silentsky0/btree-indexed-file-organization">
    </a>
</p>

<h3 align="center">
    B-Tree implementation with disk-based file operations.
</h3>

# Overview

The main premise of this
project was to visualize the index file organization used in real database
systems.

The program provides some basic tree operations: inserting, updating and
deleting a record.

This project was created for the subject of Database Structures, 5th semester of
Computer Science on Gdańsk University of Technology.

## Implementation specs

Disk operations on the index file are performed using pages (to simulate disk
access on a physical hard disk). Pages are buffered in main program memory
utilizing a vector. It can contain up to $h$ pages, so the whole branch from
root to leaf.

Disk operations on the data file are also simulated to be page(or block)-based.
Disk write or read only happen if there were a *cache miss*; only one block of
records is buffered.

# Table of contents
- [Overview](#overview)
  - [Implementation specs](#implementation-specs)
- [Table of contents](#table-of-contents)
- [Usage](#usage)
  - [Positional arguments](#positional-arguments)
  - [Instructions file](#instructions-file)
    - [Available instructions](#available-instructions)

# Usage

## Positional arguments

The program handles some positional arguments that allow choosing the operating
mode:

- `-a`, `--automatic <path>` - instructions are taken from an instructions file,
  which is described in the next section
- `-i`, `--interactive` - instructions are accepted from the console, following
  the same format as the instructions file
- `-v`, `--verbose` - prints out details about each executed instruction

This application generates some logs in `.csv` format. This file is used for
conducting performance analysis and contains every instruction and it's time of
execution.

## Instructions file

This file accepts the following format:
```
<tree degree>
<max index of randomly generated records>
<instruction 1>
<instruction 2>
...
```

### Available instructions

- `i` - insert record with a random key
- `i <key>` - insert record with a set key
- `ip <key>` - insert, then print the contents of the tree
- `d <key>` - delete record of a given key
- `dp <key>` - delete record, then print the tree
- `u <key> <identity> <name> <surname> <age>` - update a record with given data
- `p` - prints the contents of the tree
- `end` - end the execution
