/* Implementing code for Buffer Manager
Programmer's Name: Tejvir Singh And Vivek Bothra
Date: 4 Feb 2016
Programming language Java
Database USed: Minibase
Version : 1.0
*/


package bufmgr;


import global.GlobalConst;
import global.Page;
import global.PageId;


import java.util.HashMap;



/**
 * <h3>Mi ni base Buffer Manager</h3>
 * The buffer manager reads disk pages into a main memory page as needed. The
 * collection of main memory pages (called frames) used by the buffer manager
 * for this purpose is called the buffer pool. This is just an array of Page
 * objects. The buffer manager is used by access methods, heap files, and
 * relational operators to read, write, allocate, and de-allocate pages.
 */
public class BufMgr implements GlobalConst {

    /** Actual pool of pages (can be viewed as an array of byte arrays). */
    protected Page[] bufpool;

    /** Array of descriptors, each containing the pin count, dirty status, etc\
	. */
    protected FrameDesc[] frametab;

    /** Maps current page numbers to frames; used for efficient lookups. */
    protected HashMap<Integer, FrameDesc> pagemap;

    /** The replacement policy to use. */
    protected Replacer replacer;
    
  
   
    int numberOfBuffers;
    
    
    
//-------------------------------------------------------------



  /**
   * Constructs a buffer mamanger with the given settings.
   *
   * @param numbufs number of buffers in the buffer pool
   */
  public BufMgr(int numbufs) {
	    
	    
	       // This is for the current object created somewhere else in the program.. 
	     
	    bufpool = new Page[numbufs];
	    
	    numberOfBuffers = numbufs;
	    frametab = new FrameDesc[numbufs];
		for(int i = 0; i < numbufs; i++){
			frametab[i] = new FrameDesc(i);
			bufpool[i] = new Page();
		
		}
		
		pagemap = new HashMap<Integer,FrameDesc>(numbufs);
		replacer = new Clock(this);
		
	
		
  }

  /**
   * Allocates a set of new pages, and pins the first one in an appropriate
   * frame in the buffer pool.
   *
   * @param firstpg holds the contents of the first page
   * @param run_size number of pages to allocate
   * @return page id of the first new page
   * @throws IllegalArgumentException if PIN_MEMCPY and the page is pinned
   * @throws IllegalStateException if all pages are pinned (i.e. pool exceeded)
   */

  public PageId newPage(Page firstpg, int run_size) {
        
	  int pickVictimHead;
	  
		

		pickVictimHead = replacer.pickVictim();
		if(pickVictimHead != -1){
			
			PageId firstpgId ;
			
			firstpgId = global.Minibase.DiskManager.allocate_page(run_size);
			
			pinPage(firstpgId, firstpg, PIN_MEMCPY);			
		
			pagemap.put(firstpgId.pid,frametab[firstpgId.pid]);
			
			return firstpgId;
		}
		else
		{
			throw new IllegalStateException(" The Buffer pool is full ");
		}
    
    
    
  }
  
	 
	

  /**
   * Deallocates a single page from disk, freeing it from the pool if needed.
   *
   * @param pageno identifies the page to remove
   * @throws IllegalArgumentException if the page is pinned
   */
  public void freePage(PageId pageno) {
	  
	  
	  if(pagemap.get(pageno.pid).pincnt >= 1 )
	  {
		  throw new IllegalArgumentException(" Trying to free a page that is pinned ");
	  }
	  
	  
	  if(pagemap.get(pageno.pid).pincnt == 0)
	  {
		  bufpool[pagemap.get(pageno.pid).index] = null;		  
		  global.Minibase.DiskManager.deallocate_page(pageno);
		  
		  
	  }
	  
	  
	 
	 
	  
	 
  }

  /**
   * Pins a disk page into the buffer pool. If the page is already pinned, this
   * simply increments the pin count. Otherwise, this selects another page in
   * the pool to replace, flushing it to disk if dirty.
   *
   * @param pageno identifies the page to pin
   * @param page holds contents of the page, either an input or output param
   * @param skipRead PIN_MEMCPY (replace in pool); PIN_DISKIO (read the page in)
   * @throws IllegalArgumentException if PIN_MEMCPY and the page is pinned
   * @throws IllegalStateException if all pages are pinned (i.e. pool exceeded)
   */
  public void pinPage(PageId pageno, Page page, boolean skipRead) {
	  
	 int pickVictimHead;
	  
	
		 if(pagemap.get(pageno.pid) != null)
		  {
			 
			 if(pagemap.get(pageno.pid).pincnt >= 1)
		     {
				 pagemap.get(pageno.pid).pincnt++;
				 replacer.pinPage(pagemap.get(pageno.pid));
			     
		     }
			 else if(pagemap.get(pageno.pid).pincnt == 0)
			 {
				 pagemap.get(pageno.pid).pincnt++;
				 replacer.pinPage(pagemap.get(pageno.pid));
			 }
			 
		  }
		 else
		 {  
			
	         
	         pickVictimHead = replacer.pickVictim();
	         if(pickVictimHead == -1)
	         {
	        	 throw new IllegalArgumentException("Buffer is full");
	         }
	         else
	         {
	        	   if(frametab[pickVictimHead].dirty == UNPIN_DIRTY)
	        	   {
	        		   flushPage(frametab[pickVictimHead].pageno);
	        	  
	        	   }
	        	   
        		   if(pageno.pid != -1 && skipRead == PIN_DISKIO)
        		   {
        			   
        				   global.Minibase.DiskManager.read_page(pageno, bufpool[pickVictimHead]);
        				   bufpool[pickVictimHead].setPage(page);
        				   
        				   pagemap.remove(frametab[pickVictimHead].pageno.pid);
                		   
                		   frametab[pickVictimHead].pageno = pageno;
                		   replacer.pinPage(frametab[pickVictimHead]);
                		   frametab[pickVictimHead].dirty = UNPIN_CLEAN;
                		   frametab[pickVictimHead].pincnt++;
                		   
                		   pagemap.put(pageno.pid, frametab[pickVictimHead]);
        			   
        		   }
        		   else if(skipRead == PIN_MEMCPY)
        		   {
	        		   	        		  
        		       bufpool[pickVictimHead].copyPage(page);
        			   
	        		   pagemap.remove(frametab[pickVictimHead].pageno.pid);
	        		   
	        		   frametab[pickVictimHead].pageno = pageno;
	        		   replacer.pinPage(frametab[pickVictimHead]);
	        		   frametab[pickVictimHead].dirty = UNPIN_CLEAN;
	        		   frametab[pickVictimHead].pincnt++;
	        		   
	        		   pagemap.put(pageno.pid, frametab[pickVictimHead]);
	        		   
        		   }   
        		   
	        		   
	        	   
	         }
		 }
		

	
	  
    
  }

  /**
   * Unpins a disk page from the buffer pool, decreasing its pin count.
   *
   * @param pageno identifies the page to unpin
   * @param dirty UNPIN_DIRTY if the page was modified, UNPIN_CLEAN otherrwise
   * @throws IllegalArgumentException if the page is not present or not pinned
   */
  public void unpinPage(PageId pageno, boolean dirty) {
    if(pageno == null)
    {
    	throw new IllegalArgumentException(" Page no is not correct.. ");
    }
	  
	  
    
	  if(pagemap.get(pageno.pid) == null)
	  {
		  throw new IllegalArgumentException(" The page is not pinned.. and it is not in the buffer.. ");
	  }
	  else if(pagemap.get(pageno.pid).pincnt == 0)
	  {
		  throw new IllegalArgumentException(" The page is not in the buffer.. ");
	  }
	  else if(pagemap.get(pageno.pid).pincnt >= 1)
	  {
          //Means the page is pinned and is being used 
		    pagemap.get(pageno.pid).dirty = dirty;
		    pagemap.get(pageno.pid).pincnt--;
		    if(pagemap.get(pageno.pid).pincnt == 0)
		    {
		        replacer.unpinPage(pagemap.get(pageno.pid));
		    }
	  }
	  
	  
	  
   
  }

  /**
   * Immediately writes a page in the buffer pool to disk, if dirty.
*/
  public void flushPage(PageId pageno) {
    
	  
	if(frametab[pageno.pid].dirty == UNPIN_DIRTY)
	{
    Page writing_page = new Page();
    writing_page = bufpool[pageno.pid];
    
    global.Minibase.DiskManager.write_page(pageno, writing_page);
	}
	else
	{
		throw new IllegalStateException(" THe page is not dirty.. So no need to flush it.. ");
	}
    
  }

  /**
   * Immediately writes all dirty pages in the buffer pool to disk.
*/
  public void flushAllPages() {
     
	  for(int i : pagemap.keySet() )
	  {
		 flushPage(pagemap.get(i).pageno);
	  }
  }

  /**
   * Gets the total number of buffer frames.
   */
  public int getNumBuffers() {
      
	  return numberOfBuffers;
  }

  /**
   * Gets the total number of unpinned buffer frames.
*/
  public int getNumUnpinned() {
    
	  int numberOfUnpinnedPages = 0;
	  
	  for(int i=0;i<frametab.length; i++)
	  {
		  if(frametab[i].pincnt == 0)
		  {
			  numberOfUnpinnedPages++;
		  }
	  }
	  return numberOfUnpinnedPages;
  }

} // public class BufMgr implements GlobalConst
