package com.example.redminecapstoneproject.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.redminecapstoneproject.ui.testing.DonorData
import com.example.redminecapstoneproject.ui.testing.DonorDataRoom
import com.example.redminecapstoneproject.ui.testing.RegisAccountData
import com.example.redminecapstoneproject.ui.testing.RegisAccountDataRoom

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAccountData(data:RegisAccountDataRoom)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDonorData(data:DonorDataRoom)

    @Query("SELECT * FROM RegisAccountDataRoom WHERE uid= :uid")
    fun getAccountData(uid:String):LiveData<RegisAccountDataRoom>

    @Query("SELECT * FROM DonorDataRoom WHERE uid= :uid")
    fun getDonorData(uid:String):LiveData<DonorDataRoom>

    @Query("DELETE FROM RegisAccountDataRoom WHERE uid= :uid")
    fun deleteAccountData(uid:String)

    @Query("DELETE FROM DonorDataRoom WHERE uid= :uid")
    fun deleteDonorData(uid:String)
}