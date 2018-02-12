import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Reducer;
 
public class MyReducer extends Reducer<Text, Text,
                                                 Text,Text> {
 
  
  
  public void reduce(Text key, Text val, Context writer) throws IOException, InterruptedException {
    
         writer.write(key, val);
  }
 
}