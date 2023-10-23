//class Database
//This class implements the synchronization methods to be used in
//the readers writers problem
public class Database
{
   //MP2 create any variables that you need for implementation of the methods
   //of this class

   //Database
   //Initializes Database variables

   // number of readers actively reading
   int activeReaders = 0;

   //count for blockedReaders and waitingWriters
   private int blockedReaders = 0;
   private int waitingWriters = 0;

   /*
   SEMAPHORES:
   - databaseAccess -> semaphore to access the Database
   - readerCountMutex -> semaphore for readerCount
   - writerCountMutex -> semaphore for writerCount
   - blockedReadersSemaphore -> semaphore for blocked readers waiting for writers to finish
    */
   private final Semaphore databaseAccess;
   private final Semaphore readerCountMutex, writerCountMutex, blockedReadersSemaphore;



   public Database()
   {
      //MP2
      databaseAccess = new Semaphore("database_access", 1);
      readerCountMutex = new Semaphore("reader_count_mutex", 1);
      writerCountMutex = new Semaphore("writer_count_mutex", 1);
      blockedReadersSemaphore = new Semaphore("blocked_readers_semaphore", 0); //0 -> since no reader is initially waiting

   }
   //napping()
   //this is called when a reader or writer wants to go to sleep and when
   //a reader or writer is doing its work.
   //Do not change for MP2
   public static void napping()
   {
      Alarm ac = new Alarm(20);
   }

   //startRead
   //this function should block any reader that wants to read if there
   //is a writer that is currently writing.
   //it returns the number of readers currently reading including the
   //new reader.
   public int startRead()
   {
      //MP2
      //checking if writers are waiting
      writerCountMutex.P();
      int currentWaitingWriters = waitingWriters;
      writerCountMutex.V();

      //if writers are waiting -> blocking incoming readers
      if (currentWaitingWriters > 0) {
         blockedReaders++;
         blockedReadersSemaphore.P();
      }

      //incrementing active reader count
      readerCountMutex.P();
      activeReaders++;
      int currentReaders = activeReaders;

      //if this is the first readers -> acquiring the database semaphore
      if (activeReaders == 1) databaseAccess.P();

      readerCountMutex.V();

      return currentReaders;
   }

   //endRead()
   //This function is called by a reader that has finished reading from the
   //database.  It returns the current number of readers excluding the one who
   //just finished.
   public int endRead()
   {
      //MP2
      //decrementing active reader count
      readerCountMutex.P();
      activeReaders--;
      int currentReaders = activeReaders;

      //if this is the last active reader -> releasing the database semaphore
      if (activeReaders == 0) databaseAccess.V();

      readerCountMutex.V();

      return currentReaders;

   }

   //startWrite()
   //This function should allow only one writer at a time into the Database
   //and block the writer if anyone else is accessing the database for read
   //or write.
   public void startWrite()
   {
      //MP2

      //incrementing waiting writer count
      writerCountMutex.P();
      waitingWriters++;
      writerCountMutex.V();

      //acquiring exclusive access to the database
      databaseAccess.P();

      //decrementing waiting writer count
      writerCountMutex.P();
      waitingWriters--;

      //if no writers are waiting -> releasing all blocked readers
      if (waitingWriters == 0) {
         while (blockedReaders > 0) {
            blockedReadersSemaphore.V();
            blockedReaders--;
         }
      }
      writerCountMutex.V();

   }

   //endWrite()
   //signal that a writer is done writing and the database can now be accessed
   //by someone who is waiting to read or write.
   public void endWrite()
   {
      //MP2
      //releasing access to the database
      databaseAccess.V();
   }
}
