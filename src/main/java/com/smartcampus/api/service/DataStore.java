package com.smartcampus.api.service;

import com.smartcampus.api.model.Room;
import com.smartcampus.api.model.Sensor;
import com.smartcampus.api.model.SensorReading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory data store for rooms, sensors, and sensor readings.
 *
 * Thread-safety approach:
 * - ConcurrentHashMap is used for room and sensor storage to allow safe
 * concurrent reads and writes.
 * - Sensor readings are stored in ArrayList objects. Access to each readings
 * list is protected using synchronized(list) blocks during append and
 * retrieval operations.
 * - Compound operations that span two collections, such as adding a sensor and
 * updating the room's sensorIds list, are protected by a dedicated lock object
 * to prevent partial updates being visible to concurrent readers.
 * - Auto-increment ID counters use AtomicInteger to guarantee uniqueness under
 * concurrent creates.
 */

public class DataStore {

    // ---- Singleton ----
    private static final DataStore INSTANCE = new DataStore();

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // ---- Storage ----
    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // ---- ID counters ----
    private final AtomicInteger roomCounter = new AtomicInteger(10);
    private final AtomicInteger sensorCounter = new AtomicInteger(10);
    private final AtomicInteger readingCounter = new AtomicInteger(100);

    // ---- Compound-operation lock ----
    private final Object lock = new Object();

    private DataStore() {
        seedData();
    }

    // ---- Seed data ----
    private void seedData() {
        Room r1 = new Room("room-1", "Lecture Hall A", 120);
        Room r2 = new Room("room-2", "Lab Room B", 30);
        Room r3 = new Room("room-3", "Staff Office", 10);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        Sensor s1 = new Sensor("sensor-1", "TEMPERATURE", Sensor.Status.ACTIVE, 21.5, "room-1");
        Sensor s2 = new Sensor("sensor-2", "HUMIDITY", Sensor.Status.ACTIVE, 55.0, "room-1");
        Sensor s3 = new Sensor("sensor-3", "MOTION", Sensor.Status.OFFLINE, 0.0, "room-2");
        Sensor s4 = new Sensor("sensor-4", "TEMPERATURE", Sensor.Status.MAINTENANCE, 18.0, "room-3");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);
        sensors.put(s4.getId(), s4);

        r1.getSensorIds().add(s1.getId());
        r1.getSensorIds().add(s2.getId());
        r2.getSensorIds().add(s3.getId());
        r3.getSensorIds().add(s4.getId());

        List<SensorReading> r1Readings = new ArrayList<>();
        r1Readings.add(new SensorReading("reading-101", 20.1));
        r1Readings.add(new SensorReading("reading-102", 21.5));
        readings.put(s1.getId(), r1Readings);

        List<SensorReading> r2Readings = new ArrayList<>();
        r2Readings.add(new SensorReading("reading-103", 54.0));
        readings.put(s2.getId(), r2Readings);
    }

    // ---- Room operations ----

    public Collection<Room> getAllRooms() {
        return rooms.values();
    }

    public Room getRoomById(String id) {
        return rooms.get(id);
    }

    public Room createRoom(Room room) {
        String id = "room-" + roomCounter.getAndIncrement();
        room.setId(id);
        rooms.put(id, room);
        return room;
    }

    public Room updateRoom(String roomId, Room updated) {
        synchronized (lock) {
            Room existing = rooms.get(roomId);
            if (existing == null) {
                return null;
            }
            // Preserve the id and sensorIds — only allow name/capacity to change
            existing.setName(updated.getName());
            existing.setCapacity(updated.getCapacity());
            return existing;
        }
    }

    /**
     * Deletes a room. Returns false if the room does not exist; throws
     * IllegalStateException
     * (mapped to RoomNotEmptyException by the caller) if the room still has
     * sensors.
     */
    public boolean deleteRoom(String roomId) {
        synchronized (lock) {
            Room room = rooms.get(roomId);
            if (room == null) {
                return false;
            }
            if (!room.getSensorIds().isEmpty()) {
                throw new com.smartcampus.api.exception.RoomNotEmptyException(
                        "Room " + roomId + " still has " + room.getSensorIds().size()
                                + " sensor(s). Remove them first.");
            }
            rooms.remove(roomId);
            return true;
        }
    }

    // ---- Sensor operations ----

    public Collection<Sensor> getAllSensors() {
        return sensors.values();
    }

    public Sensor getSensorById(String id) {
        return sensors.get(id);
    }

    public Sensor createSensor(Sensor sensor) {
        synchronized (lock) {
            String id = "sensor-" + sensorCounter.getAndIncrement();
            sensor.setId(id);
            sensors.put(id, sensor);
            Room room = rooms.get(sensor.getRoomId());
            if (room != null) {
                room.getSensorIds().add(id);
            }
            readings.put(id, new ArrayList<>());
            return sensor;
        }
    }

    /**
     * Deletes a sensor and removes its ID from the parent room's sensorIds list.
     * Returns false if the sensor does not exist.
     */
    public boolean deleteSensor(String sensorId) {
        synchronized (lock) {
            Sensor sensor = sensors.get(sensorId);
            if (sensor == null) {
                return false;
            }
            sensors.remove(sensorId);
            // Unlink from room
            if (sensor.getRoomId() != null) {
                Room room = rooms.get(sensor.getRoomId());
                if (room != null) {
                    room.getSensorIds().remove(sensorId);
                }
            }
            // Remove readings history
            readings.remove(sensorId);
            return true;
        }
    }

    // ---- Reading operations ----

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        List<SensorReading> list = readings.get(sensorId);
        if (list == null) {
            return new ArrayList<>();
        }
        synchronized (list) {
            return new ArrayList<>(list);
        }
    }

    public SensorReading addReading(String sensorId, SensorReading reading) {
        List<SensorReading> list = readings.computeIfAbsent(sensorId, k -> new ArrayList<>());
        String id = "reading-" + readingCounter.getAndIncrement();
        reading.setId(id);
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }
        synchronized (list) {
            list.add(reading);
            // Update currentValue atomically with the reading append
            Sensor sensor = sensors.get(sensorId);
            if (sensor != null) {
                sensor.setCurrentValue(reading.getValue());
            }
        }
        return reading;
    }
}
