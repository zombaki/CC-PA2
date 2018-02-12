import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
 
import java.io.IOException;
 
public class MyMapper extends Mapper<Object, Text, Text, Text> {
  private Text word = new Text();
  private LongWritable count = new LongWritable();

  @Override
  protected void map(LongWritable in_key, Text in_value, Context context_writer)
      throws IOException, InterruptedException {
        //DECLARATION OF VARIABLES
         String  read_line=null;  
          Text key = new Text();
          Text val = new Text();
          try
          {
              read_line = in_value.toString();
              Text[] str_val=read_line.split("\\s*"); 
              key.set(str_val[0]);//FETCHING KEY FROM FILE
              val.set(str_val[1]);//FETCHING VALUE FROM FILE
              context_writer.write(key, val);//MAPPING IS DONE 
           }
      // Exception handling code
      catch(Exception e)                      
      {
        System.out.println(e);
      }
    }
  }
}