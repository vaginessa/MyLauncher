package com.xx.mylauncher;

import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity implements View.OnLongClickListener {
	
	private static final String TAG = "MainActivity";
	
	private DragLayer m_DragLayer;

	private Workspace m_Workspace;
	
	private CellLayout m_CellLayout;
	
	private DragController m_DragController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt);
		
		m_DragLayer = (DragLayer) findViewById(R.id.dragLayout);
		m_Workspace = (Workspace) findViewById(R.id.workspace);
		m_CellLayout = (CellLayout) findViewById(R.id.celllayout);
		m_DragController = new DragController(this, this );
		
		m_DragLayer.setDragController(m_DragController);
		m_DragController.registerDropTarget(m_Workspace);	//别忘记注册添加
		m_CellLayout.setLauncher(this);	//TODO 先这样添加
		m_Workspace.setDragController(m_DragController);
		m_Workspace.setDragLayer(m_DragLayer);
		m_Workspace.setOnLongClickListener(this);
		
		
	}

	public void onAddView(View view) {
		EditText edtCellX = (EditText) findViewById(R.id.edt_cellX);
		EditText edtCellY = (EditText) findViewById(R.id.edt_cellY);
		int cellHSpan = Integer.parseInt(edtCellX.getText().toString());
		int cellVSpan = Integer.parseInt(edtCellY.getText().toString());
		List<int[]> result = m_CellLayout.isAcceptAddChild(cellHSpan, cellVSpan);
		
		if (result.size() > 0) {
			int[] item = result.get(new Random().nextInt(result.size()));
			addViewInCellLayout(item[0], item[1], cellHSpan, cellVSpan);
		}

	}

	private void addViewInCellLayout(int cellX, int cellY, int cellHSpan, int cellVSpan) {
		View v = new View(this);
		CellInfo cellInfo = new CellInfo();
		cellInfo.setCellHSpan(cellHSpan);
		cellInfo.setCellVSpan(cellVSpan);
		cellInfo.setCellX(cellX);
		cellInfo.setCellY(cellY);
		cellInfo.setIconName("icon name");
		cellInfo.setIntent(new Intent());
		CellInfo.CellType type = cellHSpan > 1 || cellVSpan>1 ? CellInfo.CellType.WIDGET : CellInfo.CellType.SHORT_CUT;
		cellInfo.setType(type);
		cellInfo.setView(v);
		v.setTag(cellInfo);	//设置cellInfo
		v.setBackgroundColor(Color.RED);
		CellLayout.LayoutParams lp = new CellLayout.LayoutParams();
		lp.cellX = cellX;
		lp.cellY = cellY;
		lp.cellHSpan = cellHSpan;
		lp.cellVSpan = cellVSpan;
		v.setLayoutParams(lp);
		v.setFocusable(true);
		v.setOnLongClickListener(this);
		
		m_CellLayout.addView(v);
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
	    if (!v.isInTouchMode()) {
	        Utils.log (TAG, "isInTouchMode returned false. Try touching the view again.");
	        return false;
	     }
		
		Utils.log(true, TAG, "%s.\nview info: %s", "onLongClick", v.toString());
		
		Object tag = v.getTag();
		
		if (tag != null) {
			Utils.log(true, TAG, "%s", tag.toString());
		}
		
		if (tag instanceof CellInfo) {
			CellInfo cellInfo = (CellInfo) tag;
			Utils.log(true, TAG, "图标项或widget项. Type is %s", cellInfo.getType().name() );
			
			m_DragController.startDrag(v, m_Workspace, v.getTag(), Constant.DRAG_MOVE);
			
			return true;
			
		} else {
			Utils.log(true, TAG, "%s", "桌面空白项，弹出item列表对话框");
			
			
		}
		
		
		return false;
	}
	
	
	public DragLayer getDragLayer() {
		return m_DragLayer;
	}
	
	public Workspace getWorkspace() {
		return m_Workspace;
	}
	
	
}
