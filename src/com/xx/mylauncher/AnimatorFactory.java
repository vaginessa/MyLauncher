package com.xx.mylauncher;

import java.util.Queue;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;

import com.xx.mylauncher.CellLayout.DragObjectInfo;
import com.xx.mylauncher.CellLayout.SwapItemObject;

/**
 * 动画工厂类
 * @author baoxing
 *
 */
public class AnimatorFactory {

	
	
	/**
	 * 交换两个shortcut item view 的动画处理类
	 * @author baoxing
	 *
	 */
	static class AnimatorSwapItem {
		private static final int ANIM_ROCK_OFFSET_X = 0;
		private static final int ANIM_ROCK_OFFSET_Y = -15;
		private static final long ANIM_MOVE_HINT_DURATION = 200;
		private static final long ANIM_ROCK_HINT_DURATION = 300;
		private static final long ANIM_FOLLOW_BACK_DURATION = 150;
		private static final String TAG = "AnimatorSwapItem";
		private MainActivity m_Launcher;
		private DragLayer m_DragLayer;
		private AnimatorSet m_AnimatorSetHintRock;
		/** 克隆的对象传向DragLayer中去绘制，hint*/
		private SwapItemObject m_SwapObjectDraw = new SwapItemObject();
		/** 克隆的对象传向DragLayer中去绘制，follow back */
		private SwapItemObject m_SwapObjectBackDraw = new SwapItemObject();
		
		public AnimatorSwapItem(MainActivity launcher, DragLayer dragLayer) {
			this.m_Launcher = launcher;
			this.m_DragLayer = dragLayer;
			
		}
		
		private void initAnim(final SwapItemObject swapItemObjectDrag, final SwapItemObject swapItemObjectHint) {
//			Utils.log(TAG, "initAnim");
//			Utils.log(TAG, "swapDragInfo: %s", swapItemObjectDrag.toString() );
//			Utils.log(TAG, "swapItemHint: %s", swapItemObjectHint.toString() );
			
			m_AnimatorSetHintRock = new AnimatorSet();
			final AnimatorSet animatorSetHintRock = m_AnimatorSetHintRock;
			final SwapItemObject swapObjectDraw = m_SwapObjectDraw;
			
			ValueAnimator anim1 = new ValueAnimator();
			anim1.setDuration(ANIM_MOVE_HINT_DURATION);
			anim1.setFloatValues(0.0f, 1.0f);
			anim1.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationCancel(Animator animation) {
					super.onAnimationCancel(animation);
					swapObjectDraw.resumeValue(swapObjectDraw);
					swapItemObjectHint.resumeValue(swapObjectDraw);
				}
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					swapObjectDraw.resumeValue(swapObjectDraw);
					swapItemObjectHint.resumeValue(swapObjectDraw);
				}
				@Override
				public void onAnimationStart(Animator animation) {
					super.onAnimationStart(animation);
					swapObjectDraw.setItemViewVisiblity(View.INVISIBLE);
				}
			});
			anim1.addUpdateListener(new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					final float fProcess = (Float) animation.getAnimatedValue();
					final int iTotalX = swapItemObjectDrag.oriX - swapItemObjectHint.oriX;
					final int iTotalY = swapItemObjectDrag.oriY - swapItemObjectHint.oriY;
					final int iCurX = (int) (fProcess*iTotalX + swapItemObjectHint.oriX);
					final int iCurY = (int) (fProcess*iTotalY + swapItemObjectHint.oriY);
					
					swapItemObjectHint.curX = iCurX;
					swapItemObjectHint.curY = iCurY;
					
					swapObjectDraw.copy(swapItemObjectHint);
					m_DragLayer.updateswapItemShortCutToShortCur(swapObjectDraw);
				}
			});
			
			ValueAnimator anim2 = new ValueAnimator();
			anim2.setDuration(ANIM_ROCK_HINT_DURATION);
			anim2.setFloatValues(0.0f, 1.0f);
			anim2.setRepeatCount(ValueAnimator.INFINITE);
			anim2.setRepeatMode(ValueAnimator.REVERSE);
			anim2.addListener(new AnimatorListenerAdapter() {
			});
			anim2.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationCancel(Animator animation) {
					super.onAnimationCancel(animation);
					swapItemObjectHint.resumeValue(swapItemObjectHint);
				}
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					swapItemObjectHint.resumeValue(swapItemObjectHint);
				}
			});
			anim2.addUpdateListener(new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					final float fProcess = (Float) animation.getAnimatedValue();
					final int iTotalX = ANIM_ROCK_OFFSET_X;
					final int iTotalY = ANIM_ROCK_OFFSET_Y;
					final int iCurX = (int) (fProcess*iTotalX + swapObjectDraw.finallyX);
					final int iCurY = (int) (fProcess*iTotalY + swapObjectDraw.finallyY);
					
					swapObjectDraw.curX = iCurX;
					swapObjectDraw.curY = iCurY;
					swapItemObjectHint.curX = iCurX;
					swapItemObjectHint.curY = iCurY;
					
					m_DragLayer.updateswapItemShortCutToShortCur(swapObjectDraw);
				}
			});
			
			
			animatorSetHintRock.play(anim1);
			animatorSetHintRock.play(anim2).after(anim1);
		}
		
		private void startAnimHint() {
			Utils.log(TAG, "startAnimHint");
			m_AnimatorSetHintRock.start();
		}
		
		/**
		 * 启动移动和摇呀摇动画
		 * @param swapItemObjectDrag
		 * @param swapItemObjectHint
		 */
		public void startHintAnim(final SwapItemObject swapItemObjectDrag, final SwapItemObject swapItemObjectHint) {
			Utils.log(TAG, "startHintAnim");
			
			initAnim(swapItemObjectDrag, swapItemObjectHint);
			startAnimHint();
		}
		
		/**
		 * 取消摇呀摇动画
		 */
		public void cancelHintAnim() {
//			Utils.toastAndlogcat(m_Launcher, TAG, "cancelHintAnim");
			
			final AnimatorSet set = m_AnimatorSetHintRock;
			if (set != null) {
				if (set.isRunning() ) {
					set.cancel();
				}
			}
		}
		
		public boolean isHintAnimEnd() {
			if (m_AnimatorSetHintRock!=null && m_AnimatorSetHintRock.isStarted()) {
				return false;
			} else {
				return true;
			}
		}
		
		
		private ValueAnimator m_ValueAnimatorFollowBack;
		/**
		 * 回滚动画
		 * @param swapObjectFollow
		 * @param swapObjectHint
		 */
		public void startFollowBack(final SwapItemObject swapObjectFollow, final SwapItemObject swapObjectHint) {
			Utils.log(TAG, "startFollowBack");			
			
			final SwapItemObject swapObject = m_SwapObjectBackDraw;
			
			Utils.log(TAG, swapObjectFollow.toString() );
			
			ValueAnimator anim1 = m_ValueAnimatorFollowBack;
			
			if (anim1!=null && anim1.isStarted() ) {
				return;
			}
			
			m_ValueAnimatorFollowBack = new ValueAnimator();
			anim1= m_ValueAnimatorFollowBack;
			
			anim1.setDuration(ANIM_FOLLOW_BACK_DURATION);
			anim1.setFloatValues(0.0f, 1.0f);
			anim1.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					Utils.log(TAG, "End follow back anim end onCallBack");
//					swapObjectFollow.setItemViewVisiblity(View.VISIBLE);
//					swapObjectFollow.clearRes();
					//TODO 啥时候clearRes
					if (swapObjectHint != null) {
//						swapObjectFollow.copy(swapObjectHint);
//						swapObjectFollow = swapObjectHint;
						m_DragLayer.updateSwapFollowBackFromAnim();
					} else {
						m_DragLayer.clearSwapFollowBack();
					}
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {
					super.onAnimationCancel(animation);
					swapObjectFollow.resumeValue(swapObjectFollow);
				}
				
			});
			anim1.addUpdateListener(new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					final float fProcess = (Float) animation.getAnimatedValue();
					final int iTotalX = swapObjectFollow.oriX - swapObjectFollow.finallyX;
					final int iTotalY = swapObjectFollow.oriY - swapObjectFollow.finallyY;
					final int iCurX = (int) (fProcess*iTotalX + swapObjectFollow.finallyX);
					final int iCurY = (int) (fProcess*iTotalY + swapObjectFollow.finallyY);
					
					swapObjectFollow.curX = iCurX;
					swapObjectFollow.curY = iCurY;
					
					swapObject.copy(swapObjectFollow);
					m_DragLayer.updateswapItemShortCutToShortCurFollowBackDispatchDraw(swapObject);
				}
			});
			
			
			anim1.start();
		}
		
		
	}
	
	
	
	static class AnimatorDragFollow1 {
		
		private static final long ANIM_DRAG_FOLLOW_DURATION = 150;
//		private static final long ANIM_DRAG_FOLLOW_DURATION = 5000;
		

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
