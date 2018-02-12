import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.io.Text;
//Code written and understood from https://mapr.com/blog/how-write-mapreduce-program/
//Sort Driver is main class which takes care for handling alll calls
public class Sort_Driver
{
        //Declaration of Sort_Driver default constructor.
         public Sort_Driver()
         {
                     super();
         }
        // main method   ENTRY POINT TO CODE         
         public static void main(String[] args) throws Exception
         {
                //create Configuration object.
            Configuration conf = new Configuration();
                // Create sort_job object 
            Job job = new Job(conf, "Sort");
            job.setJarByClass(Sort_Driver.class);
              job.setMapperClass(MyMapper.class); //SET MAPPER CLASS
              job.setReducerClass(MyReducer.class); //SET REDUCER CLASS
            job.setCombinerClass(MyReducer.class);
            job.setOutputKeyClass(Text.class); //SET OUTPUT KEY
            job.setOutputValueClass(Text.class); //SET CLASS FOR VAL
            FileInputFormat.addInputPath(sort_job, new Path(args[0])); //TAKE FIRST PARAM AS INPUT
            FileOutputFormat.setOutputPath(sort_job, new Path(args[1])); //TAKE 2nd PARAM AS OUTPUT

            System.exit(sort_job.waitForCompletion(true) ? 0 : 1);

        }
}