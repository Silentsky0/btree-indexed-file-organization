package dev.silentsky.btree;

import dev.silentsky.disk.File;
import lombok.AllArgsConstructor;

import java.nio.ByteBuffer;
import java.util.Random;

public class Record {
    public int id;
    public Identity identity;
    public Data data;

    public Record(int id) {
        this.id = id;
        this.identity = new Identity("ABC", 12345);
        this.data = getRandomData();
    }

    public Record() {
        this.id = getRandomID();

        this.identity = new Identity(getRandomIdentitySeries(), getRandomIdentityNumber());

        this.data = getRandomData();
    }

    public Record(String identity_series, int identity_number, String name, String surname, int age) {
        this.id = getRandomID();
        this.identity = new Identity(identity_series, identity_number);
        this.data.name = name;
        this.data.surname = surname;
        this.data.age = age;
    }

    public static int limit;
    private static int last = 0;

    public static int getRandomID() {

        Random rand = new Random();
        return rand.nextInt(limit);

        // up to 5 digits
//        int id = (int) (System.currentTimeMillis() % limit);
//        if ( id <= last ) {
//            id = (int) ((last + 1) % limit);
//        }
//        return last = id;
    }

    int getRandomIdentityNumber() {
        return 10000 + (int)(Math.random() * ((99999 - 10000) + 1));
    }

    String getRandomIdentitySeries() {
        char[] identitySeries = "***".toCharArray();
        for (int i = 0; i < 3; i++) {
            identitySeries[i] = getRandomLetter();
        }

        return new String(identitySeries);
    }

    char getRandomLetter() {
        return (char) ('A' + (Math.random() * 26));
    }

    Data getRandomData() {
        String[] possibleNames =  {"Maurycy","Alfred","Hubert","Konrad","Krzysztof","Paweł","Krystian","Fryderyk","Heronim","Adam","Maria","Beata","Agnieszka","Katarzyna","Jagoda","Martyna","Aleksandra","Julia","Klara","Weronika"};
        String[] possibleMaleSurnames = {"Maciejewski","Szulc","Zieliński","Jaworski","Piotrowski","Czerwiński","Cieślak","Szymański","Kaczmarczyk","Kamiński"};
        String[] possibleFemaleSurnames = {"Baran","Kowalska","Woźniak","Piotrowska","Zalewska","Zawadzka","Lewandowska","Witkowska","Urbańska","Szulc"};

        String name, surname;

        int random_name_index = (int) (Math.random() * 20);
        name = possibleNames[random_name_index];

        int random_surname_index = (int) (Math.random() * 10);
        if (random_name_index < 10) {
            surname = possibleMaleSurnames[random_surname_index];
        }
        else {
            surname = possibleFemaleSurnames[random_surname_index];
        }

        return new Data(name, surname, (int) (Math.random() * 50));
    }

    public byte[] toByteArray() {
        byte[] recordBytes = new byte[BTree.getRecordBlockSize()];
        ByteBuffer buffer = ByteBuffer.wrap(recordBytes);

        buffer.putInt(id);
        buffer.putInt(identity.identitySeries.length());
        buffer.put(identity.identitySeries.getBytes());
        buffer.putInt(identity.identityNumber);

        buffer.putInt(data.name.length());
        buffer.put(data.name.getBytes());

        buffer.putInt(data.surname.length());
        buffer.put(data.surname.getBytes());

        buffer.putInt(data.age);

        return recordBytes;
    }

    public Record(byte[] recordBytes) {
        ByteBuffer buffer = ByteBuffer.wrap(recordBytes);

        this.id = buffer.getInt();

        int identitySeriesLen = buffer.getInt();
        byte[] identitySeriesBytes = new byte[identitySeriesLen];
        buffer.get(identitySeriesBytes);
        this.identity = new Identity(new String(identitySeriesBytes), buffer.getInt());

        int nameLen = buffer.getInt();
        byte[] nameBytes = new byte[nameLen];
        buffer.get(nameBytes);

        int surnameLen = buffer.getInt();
        byte[] surnameBytes = new byte[surnameLen];
        buffer.get(surnameBytes);

        this.data = new Data(new String(nameBytes), new String(surnameBytes), buffer.getInt());
    }
}

@AllArgsConstructor
class Identity {
    public String identitySeries;
    public int identityNumber;
}

@AllArgsConstructor
class Data {
    public String name;
    public String surname;
    public int age;
}

