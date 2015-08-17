package com.xx.mylauncher;

import java.util.Queue;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Rect;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;

import com.xx.mylauncher.CellLayout.DragObjectInfo;

/**
 * 动画工厂类
 * @author baoxing
 *
 */
public class AnimatorFactory {

	
	
	
	
	
	static class AnimatorDragFollow1 {
		
		private static final long ANIM_DRAG_FOLLOW_DURATION = 150;

		protected static final String TAG = "Animator2";

		private ValueAnimator m_ValueAnimatorFollowDrag;
		
		private MainActivity m_Launcher;
		
		private DragLayer m_DragLayer;
		
		private volatile boolean m_bEnableDragAnim = false;
		
		private DragObjectInfo m_DragObjectInfoCur;
		
		private DragObjectInfo m_DragObjectInfoLast;
		
		private DragObjectInfo m_DragObjectInfoNext;
		
		private Rect m_RectInvalidFollow = new Rect();
		private Rect m_RectInvalidFollow1 = new Rect();
		
		public AnimatorDragFollow1(MainActivity launcher, DragLayer dragLayer) {
			this.m_Launcher = launcher;
			this.m_DragLayer = dragLayer;
			m_ValueAnimatorFollowDrag = null;
		}
		
		
		private void initValueAnim(final DragObjectInfo dragObject) {
			m_ValueAnimatorFollowDrag = new ValueAnimator();
			m_ValueAnimatorFollowDrag.setDuration(ANIM_DRAG_FOLLOW_DURATION);
			m_ValueAnimatorFollowDrag.setInterpolator(new LinearInterpolator());
			m_ValueAnimatorFollowDrag.setFloatValues(0.0f, 1.0f);
			m_ValueAnimatorFollowDrag.addListener(new AnimatorListenerAdapter() {
				
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					
					dragFollowSequence();
				}
				
				@Override
				public void onAnimationStart(Animator animation) {
					// TODO Auto-generated method stub
					super.onAnimationStart(animation);
				}
				
			});
			m_ValueAnimatorFollowDrag.addUpdateListener(new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					final float fProcess = (Float) animation.getAnimatedValue();
					final DragObjectInfo dragObjectInfoLast = m_DragObjectInfoLast;
					final DragObjectInfo dragObjectInfoNext = m_DragObjectInfoNext;
					final int iViewWidth = m_DragObjectInfoNext.width;
					final int iViewHeight = m_DragObjectInfoNext.height;
					
					final int iOriX = dragObjectInfoLast.x;
					final int iOriY = dragObjectInfoLast.y;
					final int iFinallyX = dragObjectInfoNext.x;
					final int iFinallyY = dragObjectInfoNext.y;
					final int iTotalX = iFinallyX - iOriX;
					final int iTotalY = iFinallyY - iOriY;
					final int iCurX = (int) (iOriX + fProcess*iTotalX);
					final int iCurY = (int) (iOriY + fProcess*iTotalY);
					
					m_DragObjectInfoCur.curX = iCurX;
					m_DragObjectInfoCur.curY = iCurY;
					
					m_DragObjectInfoCur.initAnim();
					
					m_DragLayer.updateDragFollowAnim(m_DragObjectInfoCur);
//					m_DragLayer.updateDragFollowAnim(m_DragObjectInfoCur, m_RectInvalidFollow);
				}
				
			});
			
			
			
		}


		/**
		 * 更新拖拽跟随
		 * @param dragObject
		 */
		public void updateDragFollow(final DragObjectInfo dragObject) {
			m_DragObjectInfoCur = dragObject;
			
			if (m_ValueAnimatorFollowDrag == null) {
				initValueAnim(dragObject);
			}
			
			final int iQueueSize = dragObject.getFollowQueueSize();
			
			if (m_bEnableDragAnim) {
				/*
				 * 开启了一次跟随拖拽，交给动画监听去处理
				 */
				if (!m_ValueAnimatorFollowDrag.isRunning()) {
					dragFollowSequence();
				}
				
			} else {
				if (iQueueSize >= 2) {
					startFollowDrag();
				} else {
					Utils.log(TAG, "m_bEnableDragAnim=%b, draw origin", m_bEnableDragAnim);
					m_DragLayer.updateDragFollowAnim(dragObject);
				}
				
			}
			
			
		}

		private void startFollowDrag() {
			Utils.log(TAG, "startFollowDrag");
			final DragObjectInfo dragObject = m_DragObjectInfoCur;
			final ValueAnimator valueAnimator = m_ValueAnimatorFollowDrag;
			
			m_bEnableDragAnim = true;
			m_DragObjectInfoLast = dragObject.pollFollowDragObjectInfoQueue();
			m_DragObjectInfoNext = dragObject.pollFollowDragObjectInfoQueue();
			
			valueAnimator.start();
		}
		
		/**
		 * 结束一次拖拽跟随
		 */
		public void endDragFollowAnim() {
			m_bEnableDragAnim = false;
		}

		/**
		 * 当上一个动画结束时，考量是否还有下一个动画
		 */
		private void dragFollowSequence() {
			Utils.log(TAG, "dragFollowSequence");
			final int iQueueSize = m_DragObjectInfoCur.getFollowQueueSize();
			final DragObjectInfo dragObject = m_DragObjectInfoCur;

			Utils.log(TAG, "onAnimationEnd. iQueueSize=%d", iQueueSize);
			m_DragObjectInfoCur.debugFollowDragObjectQueue();
			
			if (iQueueSize > 0) {
				/*
				 * 有继续移动
				 */
				Utils.log(TAG, "keep over on");
				m_DragObjectInfoLast = (DragObjectInfo) m_DragObjectInfoNext.clone();
				m_DragObjectInfoNext = dragObject.pollFollowDragObjectInfoQueue(); 
				
				initValueAnim(dragObject);
				m_ValueAnimatorFollowDrag.start();
				
			} else {
				/*
				 * 停留在某个格子中
				 */
				m_DragLayer.updateDragFollowAnim(dragObject);
				
			}
		}
		
		public boolean isEndAnim() {
			boolean bQueueEmpty = m_DragObjectInfoCur.getFollowQueue().isEmpty();
			boolean bIsAnim = m_ValueAnimatorFollowDrag.isStarted();
			if (bQueueEmpty && !bIsAnim && !m_bEnableDragAnim) {
				return true;
			}
			
			return false;
		}
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	static class AnimatorDragFollow {
		
		/** 跟随动画 */
		private static ValueAnimator m_ValueAnimatorDragFollow;
		private static final long VALUEANIMATORDRAGFOLLOW_DURATION = 1000;
		protected static final String TAG_FOLLOW = "AnimatorFactory-follow";
		/** 最新的动画参考拖拽对象 */
		private  DragObjectInfo m_DragObjectInfoLast;
		private  DragObjectInfo m_DragObjectInfoNext;
//		private static Queue<DragObjectInfo> m_QueueDragObjectInfoLists;
		private  DragObjectInfo m_DragObjectInfoCur;
		private  DragLayer m_DragLayer;
		/** 是否开启一次跟随动画 */
		private volatile  boolean m_bEnableAnimFollow = false;
		
		/**
		 * 初始化一次跟随动画
		 */
		public  void initFollowDrag() {
			m_bEnableAnimFollow = false;
			m_ValueAnimatorDragFollow = null;
		}
		
		/**
		 * 结束和清理跟随动画
		 */
		public  void endAndClearFollowDrag() {
			if (m_ValueAnimatorDragFollow != null) {
				if (m_ValueAnimatorDragFollow.isStarted() ) {
						m_ValueAnimatorDragFollow.end();
				}
			}
			
			m_bEnableAnimFollow = false;
		}
		
		public  ValueAnimator newInstance() {
			if (m_ValueAnimatorDragFollow == null ) {
				m_ValueAnimatorDragFollow = new ValueAnimator();
				m_ValueAnimatorDragFollow.setDuration(VALUEANIMATORDRAGFOLLOW_DURATION);
				m_ValueAnimatorDragFollow.setFloatValues(0.0f, 1.0f);
				m_ValueAnimatorDragFollow.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						super.onAnimationEnd(animation);
						/*
						 * 判断是否继续启动动画
						 */
						if (m_DragObjectInfoCur.getFollowQueue() != null) {
							final int iQueueSize = m_DragObjectInfoCur.getFollowQueueSize();
							if (iQueueSize > 0) {
								/*
								 * 继续启动动画
								 */
								Utils.log(TAG_FOLLOW, "resume anim. iQueueSize=%d", iQueueSize);
								m_DragObjectInfoCur.debugFollowDragObjectQueue();
								m_DragObjectInfoLast = (DragObjectInfo) m_DragObjectInfoNext.clone();
								m_DragObjectInfoNext = m_DragObjectInfoCur.pollFollowDragObjectInfoQueue();
								m_ValueAnimatorDragFollow.start();
								
							} else {
								/*
								 * 结束一次拖拽跟随动画，不是此时结束动画，是拖拽退出或者结束的时候
								 */
								m_bEnableAnimFollow = false;
							}
							
						}
						
					}
					@Override
					public void onAnimationStart(Animator animation) {
						super.onAnimationStart(animation);
						Utils.log(TAG_FOLLOW, "onAnimationStart");
						
					}
					
				});	//end addListener
				
				
				m_ValueAnimatorDragFollow.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						
						final float fProcess = (Float) animation.getAnimatedValue();
						final int iOriX = m_DragObjectInfoLast.x;
						final int iOriY = m_DragObjectInfoLast.y;
						final int iFinallyX = m_DragObjectInfoNext.x;
						final int iFinallyY = m_DragObjectInfoNext.y;
						final int iTotalX = iFinallyX - iOriX;
						final int iTotalY = iFinallyY - iOriY;
						final int iCurX = (int) (iOriX + fProcess*iTotalX);
						final int iCurY = (int) (iOriY + fProcess*iTotalY);
						m_DragObjectInfoCur.curX = iCurX;
						m_DragObjectInfoCur.curY = iCurY;
						
						m_DragLayer.updateDragFollowAnim(m_DragObjectInfoCur);
					}
					
				});
			}
			
			return m_ValueAnimatorDragFollow;
		}
		
		/**
		 * 更新跟随拖拽
		 * @param dragObjectInfo
		 * @param dragLayer
		 */
		public  void updateDragFollow(final DragObjectInfo dragObjectInfo, final DragLayer dragLayer) {
			final int iQueueFollowsize = dragObjectInfo.getFollowQueueSize();
			m_DragLayer = dragLayer;
			m_DragObjectInfoCur = dragObjectInfo;
			
			if (m_bEnableAnimFollow) {
				//ignore
				
			} else {
				if (iQueueFollowsize >= 2) {
					//start
					m_DragObjectInfoLast = dragObjectInfo.pollFollowDragObjectInfoQueue();
					m_bEnableAnimFollow = true;
					m_DragObjectInfoNext = m_DragObjectInfoCur.pollFollowDragObjectInfoQueue();
					ValueAnimator valueAnimator = newInstance();
					valueAnimator.start();
					
				} else {
					//no start, 原地绘制
					dragObjectInfo.curX = dragObjectInfo.x;
					dragObjectInfo.curY = dragObjectInfo.y;
					dragLayer.updateDragFollowAnim(dragObjectInfo);
					
				}
				
			}
			
			
		}

	}

	
	
	
	
	
	
	
}
