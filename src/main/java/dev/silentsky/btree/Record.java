package dev.silentsky.btree;

import lombok.AllArgsConstructor;

public class Record {
    int id;
    Identity identity;
    Data data;

    public Record(int id) {
        this.id = id;
        this.identity = new Identity("ABC", 12345);
        this.data = new Data("Pawel", "Cichowski", 21);
    }
}

@AllArgsConstructor
class Identity {
    String identitySeries;
    int identityNumber;
}
@AllArgsConstructor
class Data {
    String name;
    String surname;
    int age;
}