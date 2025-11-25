package dataodev;
import java.util.ArrayList;

public class MyHashTable<K, V> {
    private HashEntry<K, V>[] table;
    private int size;
    private int capacity;
    private double loadFactorThreshold;
    private boolean usePAF; 
    private boolean useDoubleHashing; 
    private int collisionCount = 0;
    private int secondaryPrime;

    @SuppressWarnings("unchecked")
    public MyHashTable(int capacity, double loadFactorThreshold, boolean usePAF, boolean useDoubleHashing) {
        this.capacity = getNextPrime(capacity);
        this.loadFactorThreshold = loadFactorThreshold;
        this.usePAF = usePAF;
        this.useDoubleHashing = useDoubleHashing;
        updateSecondaryPrime();
        this.table = new HashEntry[this.capacity];
        this.size = 0;
    }

    private int hashFunction(K key) {
        String s = key.toString();
        int hashVal = 0;
        
        if (usePAF) {
            int z = 33;
            for (int i = 0; i < s.length(); i++) {
                // Horner kuralı ile her adımda mod alarak overflow'u engelliyoruz
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
        int res = secondaryPrime - (hashVal % secondaryPrime);
        if (res == 0) res = 1;
        return res;
    }

    public void put(K key, V value) {
        if ((double) size / capacity >= loadFactorThreshold) {
            resize();
        }

        int hashVal = hashFunction(key);
        int index = hashVal;
        int i = 0;
        int step = 1;
        
        if (useDoubleHashing) {
             step = secondHashFunction(hashVal);
        }

        while (table[index] != null) {
            if (table[index].getKey().equals(key)) {
                table[index].setValue(value);
                return;
            }

            collisionCount++;
            i++;

            // --- DÜZELTME BURADA YAPILDI ---
            if (useDoubleHashing) {
                // (hash + i*step) çok büyürse int sınırını aşıp negatif olabilir.
                index = (hashVal + i * step) % capacity;
            } else {
                index = (index + 1) % capacity;
            }
            
            // Eğer sonuç negatifse kapasite ekleyerek pozitife çevir
            if (index < 0) {
                index += capacity;
            }
            // --------------------------------

            if (i > capacity) {
                resize();
                put(key, value);
                return;
            }
        }

        table[index] = new HashEntry<>(key, value);
        size++;
    }

    public V get(K key) {
        int hashVal = hashFunction(key);
        int index = hashVal;
        int i = 0;
        int step = useDoubleHashing ? secondHashFunction(hashVal) : 1;

        while (table[index] != null) {
            if (table[index].getKey().equals(key)) {
                return table[index].getValue();
            }

            i++;
            
            // --- DÜZELTME BURADA YAPILDI ---
            if (useDoubleHashing) {
                index = (hashVal + i * step) % capacity;
            } else {
                index = (index + 1) % capacity;
            }

            // Negatif indeks koruması
            if (index < 0) {
                index += capacity;
            }
            // --------------------------------

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

        this.capacity = newCapacity;
        updateSecondaryPrime();
        this.table = new HashEntry[newCapacity];
        this.size = 0;
        
        for (HashEntry<K, V> entry : oldTable) {
            if (entry != null) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i = i + 6)
            if (n % i == 0 || n % (i + 2) == 0) return false;
        return true;
    }

    private int getNextPrime(int n) {
        if (n % 2 == 0) n++;
        while (!isPrime(n)) n += 2;
        return n;
    }

    private int getPreviousPrime(int n) {
        if (n <= 3) return 2;
        if (n % 2 == 0) n--;
        while (n > 2 && !isPrime(n)) {
            n -= 2;
        }
        return Math.max(n, 2);
    }

    private void updateSecondaryPrime() {
        if (capacity <= 3) {
            secondaryPrime = Math.max(1, capacity - 1);
            return;
        }
        secondaryPrime = getPreviousPrime(capacity - 1);
        if (secondaryPrime <= 1) {
            secondaryPrime = 1;
        }
    }
}