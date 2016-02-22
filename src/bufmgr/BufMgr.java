/* Implementing code for Buffer Manager
Programmer's Name: Tejvir Singh And Vivek Bothra
Date: 4 Feb 2016
Programming language Java
<<<<<<< HEAD
Database USed: Minibase
=======
Database USed: Minibase 
>>>>>>> c654572f589cb486f3541d582bf137f4311581b3
Version : 1.0
*/


package bufmgr;


import global.GlobalConst;
import global.Minibase;
import global.Page;
import global.PageId;



import java.util.HashMap;



/**
 * <h3>Mi ni base Buffer Manager</h3>

import java.util.HashMap;

/**
 * <h3>Minibase Buffer Manager</h3>

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
	    
	    //numberOfBuffers = numbufs;
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
	  
	  System.out.println(" In new page method.. ");
	  PageId firstpgId ;
	  
	  firstpgId = Minibase.DiskManager.allocate_page(run_size);
		
		
		
	   try{
			pinPage(firstpgId, firstpg, PIN_MEMCPY);			
	   }
	   catch(IllegalArgumentException e)
	   {
		  for(int i = 0; i <= run_size;)
		   {
			  firstpgId.pid += i;
			  Minibase.DiskManager.deallocate_page(firstpgId);
			  return null;
			
		   }
	   }
			return firstpgId;
  }
  
	 	
	



  /**
   * Deallocates a single page from disk, freeing it from the pool if needed.
   *
   * @param pageno identifies the page to remove
   * @throws IllegalArgumentException if the page is pinned
   */
  public void freePage(PageId pageno) {

	  System.out.println(" In free page method.. ");
	  FrameDesc fd = pagemap.get(pageno.pid);
	  if(fd == null)
	  {
		  throw new IllegalArgumentException(" The page is not there.. ");
	  }
	  else if(pagemap.get(pageno.pid).pincnt >= 1 )
	  {
		  throw new IllegalArgumentException(" Trying to free a page that is pinned ");
	  }
	  else if(pagemap.get(pageno.pid).pincnt == 0)
	  {
		 
		  bufpool[fd.index] = null;		  
		  fd.pincnt = 0;
		  fd.dirty = false;
		  frametab[fd.pageno.pid] = null;
		  //remove from pagemap
		  pagemap.remove(pageno);
		  Minibase.DiskManager.deallocate_page(pageno);
		   
		  
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

	 
	 System.out.println(" In pin page method.. ");
	 int pickVictimHead;
	 
	 
	 // Following lines are getting us the frame from the frametab using the pagemap.. 
	  FrameDesc fd = pagemap.get(pageno.pid);
	
	
	  
	  if(fd != null)
	  {
		  if(skipRead == PIN_MEMCPY )
		  {
			  throw new IllegalArgumentException(" Some error.. ");
		  }
		  else
		  {
			  fd.pincnt++;
			  // in this i am setting the buffer pool with that page. 
			  page.setPage(bufpool[fd.index]);
			  replacer.pinPage(fd);
		  }
	  }
	  else 
	  {
		  
		  // Now we will call for the pickvictim method..  
	      pickVictimHead = replacer.pickVictim();
	  	  if(pickVictimHead == -1)
	      {
		       throw new IllegalStateException(" The buffer pool is full.. No replacement possible.  ");
		  }
	      else
	      {
		  
		      if(skipRead == PIN_DISKIO)
		      {
			      if(frametab[pickVictimHead].dirty == true)
			      {
				      flushPage(frametab[pickVictimHead].pageno);
				  }
			     			     
				      global.Minibase.DiskManager.read_page(pageno, bufpool[pickVictimHead]);  
				      pagemap.remove(frametab[pickVictimHead].pageno.pid);
				      frametab[pickVictimHead].pincnt++;
				      page.setPage(bufpool[pickVictimHead]);
				      
       		          frametab[pickVictimHead].pageno = pageno;
       		          frametab[pickVictimHead].dirty = false;
       		          
       		          pagemap.put(pageno.pid, frametab[pickVictimHead]);
       		   	      replacer.pinPage(frametab[pickVictimHead]);
				  
			       
		      }
			      else if(skipRead == PIN_MEMCPY)
				  {
					  if(frametab[pickVictimHead].dirty == true)
					  {
						  flushPage(frametab[pickVictimHead].pageno);
					  }
					 
					       bufpool[pickVictimHead].copyPage(page);
					       // We need the page to point to the bufpool. 
					       page.setPage(bufpool[pickVictimHead]);
					       pagemap.remove(frametab[pickVictimHead].pageno.pid);
					       frametab[pickVictimHead].pincnt++;
					       
		        		   
		        		   frametab[pickVictimHead].pageno = pageno;
		        		   frametab[pickVictimHead].dirty = false;
		        		   
		        		   pagemap.put(pageno.pid, frametab[pickVictimHead]);
		        		   
		        		   replacer.pinPage(frametab[pickVictimHead]);
					  
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
	  
	  
	  System.out.println(" In unpin page method.. ");
	    FrameDesc fd = pagemap.get(pageno.pid);
	    
	    if(fd == null)
	    {
	    	throw new IllegalArgumentException("  Page is no there.. . ");
	    }
	    else if(fd.pincnt == 0)
	    {
	    	throw new IllegalArgumentException("  page is not pinned. . ");	
	    }
	    else if(fd.pincnt == 1)
	    {
	    	fd.pincnt--;
	    	fd.dirty = dirty;
	    	frametab[fd.index] = fd;
	    	pagemap.put(pageno.pid, frametab[fd.index]);
	    	replacer.unpinPage(fd);
	    }
	    else
	    {
	    	fd.pincnt--;
	    	fd.dirty = dirty;
	    	frametab[fd.index] = fd;
	    	
	    }
	    
  }

  /**
   * Immediately writes a page in the buffer pool to disk, if dirty.
*/
  public void flushPage(PageId pageno) {

	  System.out.println(" In flush page method.. ");
      FrameDesc fd = pagemap.get(pageno.pid);
  	  
	  
          Page writing_page = new Page();
          writing_page = bufpool[pageno.pid];
         if(fd.dirty == true)
         { 
        	global.Minibase.DiskManager.write_page(pageno, writing_page);
            fd.dirty = false;
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
