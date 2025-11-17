package src;


import java.util.ArrayList;

public class MyHashTable<K, V> {
    private HashEntry<K, V>[] table;
    private int size;
    private int capacity;
    private double loadFactorThreshold;
    private boolean usePAF; // true: PAF, false: SSF
    private boolean useDoubleHashing; // true: DH, false: LP
    private int collisionCount = 0;

    @SuppressWarnings("unchecked")
    public MyHashTable(int capacity, double loadFactorThreshold, boolean usePAF, boolean useDoubleHashing) {
        this.capacity = capacity;
        this.loadFactorThreshold = loadFactorThreshold;
        this.usePAF = usePAF;
        this.useDoubleHashing = useDoubleHashing;
        this.table = new HashEntry[capacity];
        this.size = 0;
    }

    private int hashFunction(K key) {
        String s = key.toString();
        int hashVal = 0;
        if (usePAF) {
            int z = 33;
            for (int i = 0; i < s.length(); i++) {
                hashVal = (hashVal * z + s.charAt(i)) % capacity;
            }
        } else {
            for (int i = 0; i < s.length(); i++) {
                hashVal += s.charAt(i);
            }
        }
        hashVal = hashVal % capacity;
        if (hashVal < 0) hashVal += capacity;
        return hashVal;
    }

    private int secondHashFunction(int hashVal) {
        int q = capacity - 1;
        while (!isPrime(q)) q--; // Kapasiteden küçük en büyük asal
        return q - (hashVal % q);
    }

    public void put(K key, V value) {
        if ((double) size / capacity >= loadFactorThreshold) resize();

        int index = hashFunction(key);
        int startIndex = index;
        int i = 0;
        int step = useDoubleHashing ? secondHashFunction(index) : 1;

        while (table[index] != null) {
            if (table[index].getKey().equals(key)) {
                table[index].setValue(value);
                return;
            }
            collisionCount++;
            i++;
            if (useDoubleHashing) index = (startIndex + i * step) % capacity;
            else index = (index + 1) % capacity;
        }

        table[index] = new HashEntry<>(key, value);
        size++;
    }

    public V get(K key) {
        int index = hashFunction(key);
        int startIndex = index;
        int i = 0;
        int step = useDoubleHashing ? secondHashFunction(index) : 1;

        while (table[index] != null) {
            if (table[index].getKey().equals(key)) return table[index].getValue();
            i++;
            if (useDoubleHashing) index = (startIndex + i * step) % capacity;
            else index = (index + 1) % capacity;
            if (i > capacity) return null; 
        }
        return null;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public ArrayList<K> getKeys() {
        ArrayList<K> keys = new ArrayList<>();
        for (HashEntry<K, V> entry : table) {
            if (entry != null) keys.add(entry.getKey());
        }
        return keys;
    }

    public int getCollisionCount() { return collisionCount; }

    @SuppressWarnings("unchecked")
    private void resize() {
        int newCapacity = getNextPrime(capacity * 2);
        HashEntry<K, V>[] oldTable = table;
        this.table = new HashEntry[newCapacity];
        this.capacity = newCapacity;
        this.size = 0;
        // collisionCount sıfırlanmaz, toplam performans ölçülüyor

        for (HashEntry<K, V> entry : oldTable) {
            if (entry != null) put(entry.getKey(), entry.getValue());
        }
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i * i <= n; i++) if (n % i == 0) return false;
        return true;
    }

    private int getNextPrime(int n) {
        while (!isPrime(n)) n++;
        return n;
    }
}