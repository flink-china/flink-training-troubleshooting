package com.dataartisans.training;

import com.dataartisans.training.entities.WindowedMeasurements;
import com.dataartisans.training.source.SourceUtils;
import com.dataartisans.training.udfs.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.DiscardingSink;

import java.util.concurrent.TimeUnit;

public class TroubledStreamingJob {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        //Time Characteristics
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.getConfig().setAutoWatermarkInterval(500);

        //Checkpointing Configuration
        env.enableCheckpointing(5000);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(4000);

        //Restart Strategy (always restart)
        env.setRestartStrategy(RestartStrategies.failureRateRestart(10, Time.of(10, TimeUnit.SECONDS), Time
                .of(1, TimeUnit.SECONDS)));


        DataStream<JsonNode> sourceStream = env.addSource(SourceUtils.createFakeKafkaSource())
                                               .assignTimestampsAndWatermarks(new MeasurementTSExtractor())
                                               .map(new MeasurementDeserializer());

        DataStream<JsonNode> enrichedStream = sourceStream.keyBy(jsonNode -> jsonNode.get("location")
                                                                                     .asText())
                                                          .map(new EnrichMeasurementByTemperature(10000));

        DataStream<WindowedMeasurements> avgValuePerLocation = enrichedStream.keyBy(jsonNode -> jsonNode.get("location")
                                                                                                        .asText())
                                                                             .timeWindow(org.apache.flink.streaming.api.windowing.time.Time.of(1, TimeUnit.SECONDS))
                                                                             .aggregate(new MeasurementAggregationFunction(), new MeasurementWindowFunction());

        avgValuePerLocation.addSink(new DiscardingSink<>()); //use for performance testing in dA Platform
//        avgValuePerLocation.print(); //use for local testing

        env.execute();
    }

}
