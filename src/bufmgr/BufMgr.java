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
//-------------------------------------------------------------



  /**
   * Constructs a buffer mamanger with the given settings.
   *
   * @param numbufs number of buffers in the buffer pool
   */
  public BufMgr(int numbufs) {



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
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Deallocates a single page from disk, freeing it from the pool if needed.
   *
   * @param pageno identifies the page to remove
   * @throws IllegalArgumentException if the page is pinned
   */
  public void freePage(PageId pageno) {
    throw new UnsupportedOperationException("Not implemented");
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
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Unpins a disk page from the buffer pool, decreasing its pin count.
   *
   * @param pageno identifies the page to unpin
   * @param dirty UNPIN_DIRTY if the page was modified, UNPIN_CLEAN otherrwise
   * @throws IllegalArgumentException if the page is not present or not pinned
   */
  public void unpinPage(PageId pageno, boolean dirty) {
    // check whether it is pinned or not
    // check whether it is in buffer pool
    // dec the pin count and write the page to disk
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Immediately writes a page in the buffer pool to disk, if dirty.
*/
  public void flushPage(PageId pageno) {
    //check if the page is dirty
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Immediately writes all dirty pages in the buffer pool to disk.
*/
  public void flushAllPages() {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Gets the total number of buffer frames.
   */
  public int getNumBuffers() {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Gets the total number of unpinned buffer frames.
*/
  public int getNumUnpinned() {
    throw new UnsupportedOperationException("Not implemented");
  }

} // public class BufMgr implements GlobalConst
