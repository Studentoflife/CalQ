package awqatty.b.DrawMath.AlignDrawParts;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.SparseArray;
import awqatty.b.DrawMath.AlignDrawBuilder;
import awqatty.b.DrawMath.DrawToCanvas.DrawForm;
import awqatty.b.ListTree.ListTree;

/*
 * Two Loops:
 * 
 * Loop 1 - Leaf-to-Root:
 * 	- calculate size of each node
 * 	- pass raw width/height up to root
 * 	- root uses size to arrange components
 * 	- root applies scale factor
 * 
 * Loop 2 - Root-to-Leaf:
 * 	- takes matrix as input from root
 * 	- applies matrix to canvas
 * 	- calls draw method for each non-leaf component
 * 	- scales canvas before drawing non-leaf components
 * 	- passes scale matrix concatenated with input matrix 
 * 		as child output matrix
 * 	- reset canvas matrix
 * 
 */
public class DrawAligned implements AlignForm {
		
	//--- Local Members ---
	private float scale = 1;
	private int color = Color.BLACK;
	
	protected AlignForm base_comp;
	protected AlignForm comp;
	private AlignSeries comp_par=null;

	//final int cflags;

	//private static SparseArray<RectF> leaf_holder = null;

	// Constructors
	public DrawAligned(AlignForm component) {
		base_comp = component;
		comp = component;
		//cflags = component.getClosureFlags();
		//Log.d("DrawAligned", "ClosureType=" + Integer.toString(cflags));
	}
	public DrawAligned(AlignForm component, int closure) {
		base_comp = component;
		comp = component;
		//cflags = (closure!=ClosureFlags.NONE ? closure : component.getClosureFlags());
		//Log.d("DrawAligned", "ClosureType=" + Integer.toString(cflags));
	}
		
	//--- Set Methods ---
	//@Override
	public void setScale(float scale_factor) {
		scale = scale_factor;
	}
	@Override
	public void setColor(int color) {
		comp.setColor(color);
		this.color = color;
	}
	
	
	//--- Manage Parentheses ---
	public void setParentheses(boolean b) {
		if (b) {
			comp_par = (AlignSeries) // TODO implement w/o casting (interface?)
					AlignDrawBuilder.buildParentheses(base_comp);
			comp_par.setColor(color);
			comp = comp_par;
		} else {
			comp_par = null;
			comp = base_comp;
		}
	}
	/*
	public <T extends DrawAligned> void assignBranchParentheses(T[] branches) {
		int i;
		int[] branch_ctypes = new int[branches.length];
		boolean[] pars_active = new boolean[branches.length];
		
		for (i=0; i<branches.length; ++i)
			branch_ctypes[i] = branches[i].cflags;
		base_comp.assignParentheses(branch_ctypes, pars_active);		
		
		for (i=0; i<pars_active.length; ++i)
			branches[i].setParentheses(pars_active[i]);
	}
	//*/
	public <T extends DrawAligned> void assignBranchParentheses(
			ListTree<T>.Navigator nav
	) {
		// Set ListTree.Navigator object
		//if (!(this instanceof T)) {
			// Throw exception?
		//}
		// NOTE: method does not check if this is of type T
		nav.moveToNode((T)this);
		// Check nav object
		if (!nav.isValid()) {
			// Throw exception?
		}
		// Build array to store decisions for each branch
		boolean[] pars_active = new boolean[nav.getNumberOfBranches()];
		// moves nav to first branch
		nav.moveToNthBranch(0);
		final ListTree<T>.Navigator nav2 = nav.newCopy();
		// gets decisions
		base_comp.subBranchShouldUsePars(nav, pars_active);
		// sets decisions
		for (int i=0; i<pars_active.length; ++i) {
			nav2.getObject().setParentheses(pars_active[i]);
			nav2.moveToEnd();
		}
	}
	
	//--- Loop Methods ---
	// Loop 1
	@Override
	public void setSubLeafSizes(List<RectF> leaf_dims) {
		comp.setSubLeafSizes(leaf_dims);
	}
	@Override
	public void getSize(RectF dst) {
		comp.getSize(dst);
		dst.left *= scale;
		dst.top *= scale;
		dst.right *= scale;
		dst.bottom *= scale;
	}

	// Loop 2
	@Override
	public void drawToCanvas(Canvas canvas, RectF dst) {
		comp.drawToCanvas(canvas, dst);
	}

	/*
	public void getLeafLocations(List<RectF> leaf_locs) {
		synchronized(DrawAligned.class) {
			if (leaf_holder == null)
				leaf_holder = new SparseArray<RectF>();	
			else leaf_holder.clear();
			
			comp.getSubLeafLocations(leaf_holder);
			final int length = leaf_holder.size();
			for (int i=0; i<length; ++i) {
				leaf_locs.add(leaf_holder.get(i));
			}
		}
	}
	//*/
	@Override
	public void getSubLeafLocations(SparseArray<RectF> leafLocs) {
		comp.getSubLeafLocations(leafLocs);
	}
	@Override
	// Note: Has no use in DrawAligned objects
	public <T extends DrawAligned> void subBranchShouldUsePars(
			ListTree<T>.Navigator nav,
			boolean[] pars_active) {}

	// On Touch Loop
	@Override
	public boolean intersectsTouchRegion(RectF dst, float px, float py) {
		return comp.intersectsTouchRegion(dst, px, py);
	}
	@Override
	public boolean intersectsTouchRegion(
			RectF dst,
			float p1_x, float p1_y,
			float p2_x, float p2_y
	) {
		return comp.intersectsTouchRegion(dst, p1_x, p1_y, p2_x, p2_y);
	}

	// Other Methods
	@Override
	public void clearCache() {
		//synchronized(DrawAligned.class) {
		//	leaf_holder = null;
		//}
		if (comp != null)
			comp.clearCache();
	}
}
