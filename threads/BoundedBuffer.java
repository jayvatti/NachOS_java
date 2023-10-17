//class BoundedBuffer
//This class implements the synchronization methods to be used in 
//the bounded buffer problem 

public class BoundedBuffer
{
   //MP2 create any variables you need
   Semaphore full, empty, mutex;
   char queue[];
   int length;
   int in = 0, out = 0;

   //BoundedBuffer
   //constructor:  initialize any variables that are needed for a bounded
   //buffer of size "size"
   public BoundedBuffer(int size)
   {
      length = size;
      queue = new char[size];
      full = new Semaphore("full_semaphore",0);
      empty = new Semaphore("empty_semaphore",size);
      mutex = new Semaphore("mutex_semaphore",1);

   }

   //produce()
   //produces a character c.  If the buffer is full, wait for an empty
   //slot
   public void produce(char c)
   {
      //MP2
      empty.P(); //decrement the no. of empty spaces -> wait if the buffer is full i.e #empty == 0
      mutex.P(); //acquire the lock
      queue[in] = c; //insert item to the buffer;
      in = (in+1) % length;
      mutex.V(); //release the lock
      full.V();  //increment the number of full spaces
   }

   //consume()
   //consumes a character.  If the buffer is empty, wait for a producer.
   //use method SynchTest.addToOutputString(c) upon consuming a character.
   //This is used to test your implementation.
   public void consume()
   {
      //MP2
      //make sure you change the following line accordingly
      full.P(); //decrement the no. of full spaces -> wait if the buffer is empty i.e #full == 0
      mutex.P();
      char consumedChar = queue[out];
      out = (out + 1) % length;
      mutex.V();  //release the lock
      empty.V();  //increment the number of full spaces
      SynchTest.addToOutputString(consumedChar); //for testing
   }

}
