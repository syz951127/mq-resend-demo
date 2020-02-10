package com.sucheon.ddps.rabbitmqdemo.mapper;

import com.sucheon.ddps.rabbitmqdemo.model.MsgModel;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface MsgMapper extends Mapper<MsgModel> {

    @Select("select * from msg_log where status != 3 and status != 0 ")
    List<MsgModel> selectFailMsg();

    @Insert("INSERT INTO msg_log ( msg_id,msg,exchange,routing_key,status,try_count,next_try_time,create_time,update_time ) VALUES( ?,?,?,?,?,?,?,?,? ) ")
    void saveOne();

    @Update("update msg_log set status = #{status},try_count=#{tryCount},update_time = #{updateTime} where msg_id = #{msgId}")
    void updateStatus(MsgModel msgModel);

    @Select("select * from msg_log where msg_id = #{msgId}")
    MsgModel getOne(String msgId);
}
