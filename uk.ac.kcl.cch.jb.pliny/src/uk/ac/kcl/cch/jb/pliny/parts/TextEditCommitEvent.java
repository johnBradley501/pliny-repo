package uk.ac.kcl.cch.jb.pliny.parts;

public class TextEditCommitEvent {
   private IDirectEditablePart mypart;
private int traverseDetail;

   public TextEditCommitEvent(IDirectEditablePart p, int traverseDetail){
	   this.mypart = p;
	   this.traverseDetail = traverseDetail;
   }
   
   public IDirectEditablePart getMyEditablePart(){
	   return mypart;
   }
   
   public int getMyTraverseDetail(){
	   return traverseDetail;
   }
}
