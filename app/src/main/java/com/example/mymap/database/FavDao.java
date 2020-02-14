package com.example.mymap.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


import com.example.mymap.model.UserData;

import java.util.List;

@Dao
public interface FavDao {

    @Query("SELECT * FROM Usernotes ORDER BY ID")
    List<UserData> loadAllPersons();

    @Insert
    void insertPerson(UserData userData);

    @Update
    void updatePerson(UserData userData);

    @Delete
    void delete(UserData userData);

    @Query("SELECT * FROM Usernotes WHERE id = :id")
    UserData loadPersonById(int id);
}
