package com.xx.mylauncher;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.xx.mylauncher.CellLayout.DragObjectInfo;

/**
 * 动画工厂类
 * @author baoxing
 *
 */
public class AnimatorFactory {

	/**
	 * 交换图标要用到的动画线程池
	 * @author baoxing
	 *
	 */
	static class SwapAnimatorPool {
		/** AnimatorSet 线程池的大小 */
		private static  int I_ANIMATOR_SET_POOL_SIZE = 3; 
		
		/** ValueAnimator 线程池的大小 */
		private static  int I_VALUEANIMATOR_POOL_SIZE = 7;
		
		/** 线程池扩容的大小 */
		private static final int I_INCREATE_CAPACITY = 3;
		
		private static final List<Boolean> m_ListAnimatorSetFlag = new ArrayList<Boolean>(I_ANIMATOR_SET_POOL_SIZE);
		private static final List<AnimatorSet> m_ListAnimatorSet = new ArrayList<AnimatorSet>(I_ANIMATOR_SET_POOL_SIZE);
		
		private static final List<Boolean>  m_ListValueAnimatorFlag = new ArrayList<Boolean>(I_VALUEANIMATOR_POOL_SIZE);
		private static final List<ValueAnimator> m_ListValueAnimator = new ArrayList<ValueAnimator>(I_VALUEANIMATOR_POOL_SIZE);

		private static final String TAG = "SwapAnimatorPool";
		
		private static SwapAnimatorPool m_Instance;
		
		private SwapAnimatorPool() {
			for (int i=0; i<I_ANIMATOR_SET_POOL_SIZE; i++) {
				AnimatorSet item = new AnimatorSet();
				m_ListAnimatorSet.add(item);
				m_ListAnimatorSetFlag.add(Boolean.FALSE);
			}
			
			for (int i=0; i<I_VALUEANIMATOR_POOL_SIZE; i++) {
				ValueAnimator item = new ValueAnimator();
				m_ListValueAnimator.add(item);
				m_ListValueAnimatorFlag.add(Boolean.FALSE);
			}
		}
		
		private void increaseCapacityAnimatorSet() {
			Utils.log(TAG, "increase capacity animator set. size=%d", I_ANIMATOR_SET_POOL_SIZE);			
			
			int iPreSize = I_ANIMATOR_SET_POOL_SIZE;
			
			I_ANIMATOR_SET_POOL_SIZE += I_INCREATE_CAPACITY;
			((ArrayList<AnimatorSet>)m_ListAnimatorSet).ensureCapacity(I_ANIMATOR_SET_POOL_SIZE);
			((ArrayList<Boolean>)m_ListAnimatorSetFlag).ensureCapacity(I_ANIMATOR_SET_POOL_SIZE);
			
			for (int i=iPreSize; i<I_ANIMATOR_SET_POOL_SIZE; i++) {
				m_ListAnimatorSet.add(new AnimatorSet());
				m_ListAnimatorSetFlag.add(Boolean.FALSE);
			}
			
		}
		private void increaseCapacityValueAnimator() {
			Utils.log(TAG, "increase capacity value animator. size=%d", I_VALUEANIMATOR_POOL_SIZE);
			
			int iPreSize = I_VALUEANIMATOR_POOL_SIZE;
			
			I_VALUEANIMATOR_POOL_SIZE += I_INCREATE_CAPACITY;
			((ArrayList<ValueAnimator>)m_ListValueAnimator).ensureCapacity(I_VALUEANIMATOR_POOL_SIZE);
			
			for (int i=iPreSize; i<I_VALUEANIMATOR_POOL_SIZE; i++) {
				m_ListValueAnimator.add(new ValueAnimator());
				m_ListValueAnimatorFlag.add(Boolean.FALSE);
			}
		}
		
		
		/** 获取单例 */
		public static SwapAnimatorPool getInstance() {
			if (m_Instance==null) {
				m_Instance = new SwapAnimatorPool();
			}
			
			return m_Instance;
		}

		/** 获取空闲的位置，如果是-1，则表示没有空闲的ValueAnimator */
		private int getIdlePostionAnimatorSet() {
			int iPostion = 0;
			for (Boolean item : m_ListAnimatorSetFlag) {
				if (item.booleanValue() == false) {
					break;
				}
				iPostion++;
			}
			
			if (iPostion >= m_ListAnimatorSetFlag.size()) {
				iPostion = -1;
			}
			
			return iPostion;
		}
		/** 获取空闲的位置，如果是-1，则表示没有空闲的ValueAnimator */
		private int getIdlePostionValueAnimator() {
			int iPostion = 0;
			for (Boolean item : m_ListValueAnimatorFlag) {
				if (item.booleanValue() == false) {
					break;
				}
				iPostion++;
			}
			
			if (iPostion >= m_ListValueAnimatorFlag.size()) {
				iPostion = -1;
			}
			
			return iPostion;
		}
		
		
		/** 获取池中的ValueAnimator */
		public ValueAnimator getValueAnimator() {
			Utils.log(TAG, "getValueAnimator");
			
			int iPositon = getIdlePostionValueAnimator();
			if (iPositon == -1) {
				iPositon = m_ListValueAnimator.size();
				increaseCapacityValueAnimator();
			}
			
			m_ListValueAnimatorFlag.set(iPositon, Boolean.TRUE);
			ValueAnimator valueAnimator = m_ListValueAnimator.get(iPositon);
			valueAnimator.removeAllListeners();
			valueAnimator.removeAllUpdateListeners();
			
			//TODO 这里的线程池实现有问题
			return valueAnimator;
//			return new ValueAnimator();
		}
		
		
		/** 获取池中的AnimatorSet */
		public AnimatorSet getAnimatorSet() {
			Utils.log(TAG, "getAnimatorSet");
			
			int iPositon = getIdlePostionAnimatorSet();
			if (iPositon == -1) {
				iPositon = m_ListAnimatorSet.size();
				increaseCapacityAnimatorSet();
			}					
			
			m_ListAnimatorSetFlag.set(iPositon, Boolean.TRUE);
			AnimatorSet set =  m_ListAnimatorSet.get(iPositon);
			ArrayList<Animator> childAnimations = set.getChildAnimations();
			set.removeAllListeners();
			
			for (Animator item : childAnimations) {
				item.removeAllListeners();
				if (item instanceof ValueAnimator) {
					((ValueAnimator)item).removeAllUpdateListeners();
				}
			}
			
			return set;
//			return new AnimatorSet();
		}
		
		
		public void relaseAnimatorSet(AnimatorSet set) {
			Utils.log(TAG, "relaseAnimatorSet");
			
			int iPostion = m_ListAnimatorSet.indexOf(set);
			if (iPostion != -1) {
				m_ListAnimatorSetFlag.set(iPostion, Boolean.FALSE);
				Utils.log(TAG, "relase success position=%d", iPostion);
			}
			
		}
		
		public void relaseValueAnimator(ValueAnimator valueAnimator) {
			Utils.log(TAG, "relaseValueAnimator");			
			
			int iPositon = m_ListValueAnimator.indexOf(valueAnimator);
			if (iPositon != -1) {
				m_ListValueAnimatorFlag.set(iPositon, Boolean.FALSE);
				Utils.log(TAG, "realese ValueAnimator succeed");				
			}
		}
		
		
	}
	
	/**
	 * 交换动画
	 * @author baoxing
	 *
	 */
	static class AnimatorSwapItem {
		private static final long L_AINM_HINT_MOVE_DURATION = 400;

		private static final long L_ANIM_HINT_ROCK_DURATION = 300;

		protected static final int I_ANIM_HINT_ROCK_OFFSET_X = 0;

		protected static final int I_ANIM_HINT_ROCK_OFFSET_Y = -15;

		private static final long L_ANIM_FALLBACK_DURATION = 300;

		private static final String TAG = "AnimatorSwapItem";

		/** 动画线程池 */
		private static SwapAnimatorPool m_SwapAnimatorPool;
		
		private static AnimatorSwapItem m_Instance;
		
		private static DragLayer m_DragLayer;
		private static MainActivity m_Launcher;
		
		/** hint 列表，也是在DragLayer中的引用 */
		private static List<SwapItemObject> m_ListHintObject;
		/** fallback列表，也是在DragLayer中的引用 */
		private static List<SwapItemObject> m_ListFallBackOnDrawObject;
		/** fallback列表，保存最新的fallback元素 */
		private List<SwapItemObject> m_ListFallBackObject = new ArrayList<AnimatorFactory.SwapItemObject>();
		
		private SwapItemObject m_SwapItemObjectLastHint;
		
		private AnimatorSwapItem() {
			m_SwapAnimatorPool = SwapAnimatorPool.getInstance();
		}
		
		public static AnimatorSwapItem getInstance(DragLayer dragLayer, List<SwapItemObject> listHintObject, List<SwapItemObject> listFallBackObject, 
									MainActivity launcher) {
			if (m_Instance == null) {
				m_Instance = new AnimatorSwapItem();
				m_DragLayer = dragLayer;
				m_ListHintObject = listHintObject;
				m_ListFallBackOnDrawObject = listFallBackObject;
				m_Launcher = launcher;
			}
			
			return m_Instance;
		}
		
		public void startHintAnim(final SwapItemObject dragObject, final SwapItemObject hintObject) {
			Utils.log(TAG, "startHintAnim");
			
			m_SwapItemObjectLastHint = hintObject;
			
			if (hintObject != null) {
				final SwapItemObject findObject = findFallbackObjectInFallBackList(hintObject);
				if (findObject != null) {
					Animator animator = findObject.animator;
					animator.cancel();
					removeObjectInFallBackList(findObject);
//					hintObject.oriX = findObject.curX;
//					hintObject.oriY = findObject.curY;
					hintObject.finallyX = findObject.curX;
					hintObject.finallyY = findObject.curY;
				} else {
					hintObject.finallyX = hintObject.oriX;
					hintObject.finallyY = hintObject.oriY;
				}
				
				AnimatorSet anim = initHintAnim(dragObject, hintObject);
				anim.start();
			} 
			
		}

		
		public void startFallbackAnim(final SwapItemObject hintObject) {
			Utils.log(TAG, "startFallbackAnim");
			
			final List<SwapItemObject> listFallback = m_ListFallBackObject;
			final List<SwapItemObject> listNewFallback = getNewFallbackObject();
			
			Utils.log(TAG, "listNewFallback size=%d", listNewFallback.size());
			
			if (hintObject != null) {
				if (!containsInListFallBack(hintObject)) {
					listFallback.add((SwapItemObject) hintObject.clone());
				}
			}
			
			adjustFallbackObject(listNewFallback);
			initAnimFallback(listNewFallback);
			startAnimFallback(listNewFallback);
		}
		
		private void startAnimFallback(List<SwapItemObject> listNewFallback) {
			Utils.log(TAG, "startAnimFallback");
			
			for (SwapItemObject item : listNewFallback) {
				item.animator.start();
			}
		}
		
		private void removeObjectInHintList(final SwapItemObject removeObject) {
			Utils.log(TAG, "removeObjectInHintList");
			
			List<SwapItemObject> listHintObject = m_ListHintObject;
			SwapItemObject findObject = null;
			
			for (SwapItemObject item : listHintObject) {
				if (item.isTheSameObject(removeObject)) {
					findObject = item;
					break;
				}
			}
			
			if (findObject != null) {
				listHintObject.remove(findObject);
				Utils.log(TAG, "remove success from listHintObjects");
			}
		}
		
		/**
		 * 从回落列表中删除元素
		 * @param removeObject
		 */
		private void removeObjectInFallBackList(final SwapItemObject removeObject) {
			Utils.log(TAG, "removeObjectInFallBackList");
			
			final List<SwapItemObject> listFallback = m_ListFallBackObject;
			final List<SwapItemObject> listFallbackOnDraw = m_ListFallBackOnDrawObject;
			
			SwapItemObject findObject = null;
			for (SwapItemObject item : listFallback) {
				if (item.isTheSameObject(removeObject)) {
					findObject = item;
					break;
				}
			}
			if (findObject != null) {
				Utils.log(TAG, "delete item from listFallback");
				listFallback.remove(findObject);
			}
			
			
			findObject = null;
			for (SwapItemObject item : listFallbackOnDraw) {
				if (item.isTheSameObject(removeObject)) {
					findObject = item;
					break;
				}
			}
			if (findObject != null) {
				Utils.log(TAG, "delete item from listFallbackOnDraw");
				listFallbackOnDraw.remove(findObject);
			}
			
		}

		/**
		 * 在ListFallBack列表中是否包含指定的SwapItemObject
		 * 因为包含的条件自定义，所以要自己写
		 * @param hintObject
		 * @return
		 */
		private boolean containsInListFallBack(SwapItemObject hintObject) {
			final List<SwapItemObject> listFallBack = m_ListFallBackObject;
			
			boolean bContains = false;
			for (SwapItemObject item : listFallBack) {
				if (item.isTheSameObject(hintObject) ) {
					
					bContains = true;
					break;
				}

			}
			
			Utils.log(TAG, "是否在listFallback中包含=%b", bContains);
			return bContains;
		}
		
		/**
		 * 因为listfallback和listfallbackOndraw的元素不是相同的引用，但是listfallback的元素设置设置类animator,
		 * 而listfallback中没有
		 * @param listNewFallback
		 */
		private void mappingAnimInListBack(final List<SwapItemObject> listNewFallback) {
			final List<SwapItemObject> listFallback = m_ListFallBackObject;
			
			for (SwapItemObject srcItem : listNewFallback) {
				for (SwapItemObject dstItem : listFallback) {
					if (srcItem.isTheSameObject(dstItem)) {
						//TODO 这里好奇怪
//						dstItem = (SwapItemObject) srcItem.clone();
						dstItem.animator = srcItem.animator;
//						dstItem = srcItem;
					}
				}
			}
			
		}
		
		/**
		 * 初始化和启动回落动画
		 * @param listNewFallback
		 */
		private void initAnimFallback(List<SwapItemObject> listNewFallback) {
			Utils.log(TAG, "initAimFallback");			
			
			final SwapAnimatorPool animatorPool = m_SwapAnimatorPool;
			final List<SwapItemObject> listFallbackOnDraw = m_ListFallBackOnDrawObject;
			
			for (final SwapItemObject item : listNewFallback) {
				final ValueAnimator anim = animatorPool.getValueAnimator();
				item.animator = anim;
				anim.setDuration(L_ANIM_FALLBACK_DURATION);
				anim.setFloatValues(0.0f, 1.0f);
				anim.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationStart(Animator animation) {
						super.onAnimationStart(animation);
						Utils.log(TAG, "anim fall back start");
						Utils.log(TAG, "debug:\n%s", item.toString());
						m_Launcher.getWorkspace().getCurCellLayout().flagOcuped((CellInfo) item.itemView.getTag());
//						item.setItemVisibility(View.VISIBLE);
						listFallbackOnDraw.add(item);
					}
					@Override
					public void onAnimationEnd(Animator animation) {
						super.onAnimationEnd(animation);
						Utils.log(TAG, "anim fall back end");
						
						item.finallyX = item.curX;
						item.finallyY = item.curY;
						item.setItemVisibility(View.VISIBLE);
						animatorPool.relaseValueAnimator((ValueAnimator) item.animator);
						removeObjectInFallBackList(item);
					}
					@Override
					public void onAnimationCancel(Animator animation) {
						super.onAnimationCancel(animation);
					}
				});
				anim.addUpdateListener(new AnimatorUpdateListener() {
					
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						final float fProcess = (Float) animation.getAnimatedValue();
						final int iTotalX = item.oriX - item.finallyX;
						final int iTotalY = item.oriY - item.finallyY;
						final int iCurX = (int) (item.finallyX + fProcess*iTotalX);
						final int iCurY = (int) (item.finallyY + fProcess*iTotalY);
						
						item.curX = iCurX;
						item.curY = iCurY;
						item.isInvalid = true;
						//TODO dragLayer 回退操作
						m_DragLayer.updateSwapItemAnimFallback();
					}
				});
			}
			
//			mappingAnimInListBack(listNewFallback);
		}

		/**
		 * 调整回落的item
		 * @param listNewFallback
		 */
		private void adjustFallbackObject(List<SwapItemObject> listNewFallback) {
			final List<SwapItemObject> listHintObjects = m_ListHintObject;
			
			for (SwapItemObject item : listNewFallback) {
				SwapItemObject findObject = findSwapItemObjectInHintList(item);
				Animator animator;
				if (findObject != null) {
					Utils.log(TAG, "adjustFallbackObject-调整");					
					animator = findObject.animator;
					animator.cancel();
					listHintObjects.remove(findObject);
					item.finallyX = findObject.finallyX;
					item.finallyY = findObject.finallyY;
					
				}
			}
		}

		/**
		 * 返回是否在ListHintObject中有该对象，即是否正在运动
		 * @param object
		 * @return
		 */
		private SwapItemObject findSwapItemObjectInHintList(SwapItemObject object) {
			final List<SwapItemObject> listHintObjects = m_ListHintObject;
			SwapItemObject resultObject = null;
			for (SwapItemObject item : listHintObjects) {
				if (item.isTheSameObject(object)) {
					resultObject = item;
					break;
				}
			}
			
			
			return resultObject;
		}
		
		
		/**
		 * 得到新的要回落的对象
		 * @return
		 */
		private List<SwapItemObject> getNewFallbackObject() {
			final List<SwapItemObject> listHintObjects = m_ListHintObject;
			final List<SwapItemObject> listFallBack = m_ListFallBackObject;
			final List<SwapItemObject> listFallBackOnDraw = m_ListFallBackOnDrawObject;
			
	
			
			List<SwapItemObject> listInterSection = getIntersection(listFallBack, listHintObjects);
			List<SwapItemObject> listDiffSection = getDiffSection(listFallBackOnDraw, listInterSection);	//这个应该是只有一个的
			
			
			Utils.log(TAG, "getNewFallbackObject. listHintObjects.size=%d, listFallBack=%d, listFallBackOnDraw=%d", listHintObjects.size(), listFallBack.size(),
					listFallBackOnDraw.size());
			Utils.log(TAG, "listDiffSection.size=%d", listDiffSection.size());
			
			return listDiffSection;
		}
		
		/**
		 * 得到两个列表的交集
		 * @param list1
		 * @param list2
		 * @return
		 */
		private List<SwapItemObject> getIntersection(List<SwapItemObject> list1, List<SwapItemObject> list2) {
			List<SwapItemObject> interSectionList = new ArrayList<AnimatorFactory.SwapItemObject>();
			for (SwapItemObject item1 : list1) {
				for (SwapItemObject item2 : list2) {
					if (item1.isTheSameObject(item2)) {
						interSectionList.add(item1);
					}
					
				}
			}
			
			return interSectionList;
		}
		
		/**
		 * 得到list1的补集
		 * 即在list2中有，而list1中没有
		 * 
		 * @param list1
		 * @param list2
		 * @return
		 */
		private List<SwapItemObject> getDiffSection(List<SwapItemObject> list1, List<SwapItemObject> list2) {
			List<SwapItemObject> diffSection = new ArrayList<AnimatorFactory.SwapItemObject>();
			
			OUT:
			for (SwapItemObject item1 : list2) {
				for (SwapItemObject item2 : list1) {
					if (item1.isTheSameObject(item2) ) {
						continue OUT;
					}
					
				}
//				diffSection.add((SwapItemObject) item1.clone());
				diffSection.add(item1);
			}
			
			
			return diffSection;
		}
		
		

		private AnimatorSet initHintAnim(final SwapItemObject dragObject, final SwapItemObject hintObject) {
			Utils.log(TAG, "initHintAnim");
			
			
			final List<SwapItemObject> listHintObject = m_ListHintObject;
			final SwapAnimatorPool animatorPool = m_SwapAnimatorPool;
			
			final AnimatorSet animatorSet = animatorPool.getAnimatorSet();
			final SwapItemObject swapItemCloned = (SwapItemObject) hintObject.clone();
			
			swapItemCloned.animator = animatorSet;
			animatorSet.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation) {
					Utils.log(TAG, "animation start hintObject");
					super.onAnimationStart(animation);
					m_Launcher.getWorkspace().getCurCellLayout().clearFlagsOcupid((CellInfo) swapItemCloned.itemView.getTag());
					swapItemCloned.setItemVisibility(View.INVISIBLE);
					listHintObject.add(swapItemCloned);
				}
				@Override
				public void onAnimationEnd(Animator animation) {
					Utils.log(TAG, "animation end hintObject");
					super.onAnimationEnd(animation);
					swapItemCloned.finallyX = swapItemCloned.curX;
					swapItemCloned.finallyY = swapItemCloned.curY;
					animatorPool.relaseAnimatorSet((AnimatorSet) swapItemCloned.animator);
					removeObjectInHintList(swapItemCloned);
				}
				@Override
				public void onAnimationCancel(Animator animation) {
					super.onAnimationCancel(animation);
				}
				
			});
			
			ValueAnimator anim1 = animatorPool.getValueAnimator();
			anim1.setDuration(L_AINM_HINT_MOVE_DURATION);
			anim1.setFloatValues(0.0f, 1.0f);
			anim1.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					swapItemCloned.finallyX = swapItemCloned.curX;
					swapItemCloned.finallyY = swapItemCloned.curY;
				}
				@Override
				public void onAnimationCancel(Animator animation) {
					super.onAnimationCancel(animation);
				}
			});
			anim1.addUpdateListener(new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					final float fProcess = (Float) animation.getAnimatedValue();
//					final int iTotalX = dragObject.oriX - swapItemCloned.oriX;
//					final int iTotalY = dragObject.oriY - swapItemCloned.oriY;
//					final int iCurX = (int) (swapItemCloned.oriX + fProcess*iTotalX);
//					final int iCurY = (int) (swapItemCloned.oriY + fProcess*iTotalY);
					final int iTotalX = dragObject.oriX - swapItemCloned.finallyX;
					final int iTotalY = dragObject.oriY - swapItemCloned.finallyY;
					final int iCurX = (int) (swapItemCloned.finallyX + fProcess*iTotalX);
					final int iCurY = (int) (swapItemCloned.finallyY + fProcess*iTotalY);
					
					
					swapItemCloned.curX = iCurX;
					swapItemCloned.curY = iCurY;
					swapItemCloned.isInvalid = true;
					
					//TODO dragLayer去更新提醒
					m_DragLayer.updateSwapItemAnimHint();
				}
			});
			
			ValueAnimator anim2 = animatorPool.getValueAnimator();
			anim2.setDuration(L_ANIM_HINT_ROCK_DURATION);
			anim2.setFloatValues(0.0f, 1.0f);
			anim2.setRepeatCount(ValueAnimator.INFINITE);
			anim2.setRepeatMode(ValueAnimator.REVERSE);
			anim2.addUpdateListener(new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					final float fProcess = (Float) animation.getAnimatedValue();
					final int iTotalX = I_ANIM_HINT_ROCK_OFFSET_X;
					final int iTotalY = I_ANIM_HINT_ROCK_OFFSET_Y;
					final int iCurX = (int) (swapItemCloned.finallyX + fProcess*iTotalX);
					final int iCurY = (int) (swapItemCloned.finallyY + fProcess*iTotalY);
					
					swapItemCloned.curX = iCurX;
					swapItemCloned.curY = iCurY;
					swapItemCloned.isInvalid = true;
					
					//TODO dragLayer 去更新，draglayer中有该列表索引
					m_DragLayer.updateSwapItemAnimHint();
				}
			});
			
			animatorSet.play(anim1);
			animatorSet.play(anim2).after(anim1);
			
			return animatorSet;
		}
		
		/**
		 * 查找在fallBacklist中是否有该对象
		 * @param object
		 * @return
		 */
		private SwapItemObject findFallbackObjectInFallBackList(final SwapItemObject object) {
			SwapItemObject resultObject = null;
			
			if (object != null) {
				for (SwapItemObject item : m_ListFallBackObject) {
					if (item.isTheSameObject(object)) {
						resultObject = item;
						break;
					}
				}
				
			}			
			
			return resultObject;
		}
		
		/**
		 * 当释放时，有需要，则取消hint list 中的动画
		 */
		private void clearHintAnim() {
			final List<SwapItemObject> listHintObjects = m_ListHintObject;
			final List<SwapItemObject> listFallback = m_ListFallBackObject;
			final List<SwapItemObject> listFallbackOnDraw = m_ListFallBackOnDrawObject;
			
			for (SwapItemObject item : listHintObjects) {
				item.animator.end();
			}
			
			listHintObjects.clear();
			listFallback.clear();
			listFallbackOnDraw.clear();
		}
		
		public void swapItemOnComplete(final DragSource source, final boolean success, final DragObjectInfo dragObject) {
			swapItemOnComplete(source, success, dragObject, m_SwapItemObjectLastHint);
		}
		
		/**
		 * 当拖拽结束时
		 * @param source
		 * @param success
		 */
		private void swapItemOnComplete(final DragSource source, final boolean success, final DragObjectInfo dragObject, final SwapItemObject lastObject) {
			//TODO
			final MainActivity launcher = m_Launcher;
			final LauncherDBManager dbManager = launcher.getLauncherDBManager();
			final HotSeat hotSeat = launcher.getHotSeat();
			final CellLayout curCellLayout =launcher.getWorkspace().getCurCellLayout();
			
			if (source instanceof HotSeat) {
				if (success) {
					/*
					 * 1. 取消hint anim
					 * 2. 修改last item view,到HotSeat
					 * 3. 加入数据库
					 */
					Utils.log(TAG+"swap", "source=HotSeat, true, success=%b", success);
					
					if (lastObject != null) {
						Utils.log(TAG+"swap", "lastObject not null");
						final CellInfo lastCellInfo = (CellInfo) lastObject.itemView.getTag();
						final CellInfo dragCellInfo = (CellInfo) dragObject.itemView.getTag();
						final CellLayout.LayoutParams lp = (com.xx.mylauncher.CellLayout.LayoutParams) lastObject.itemView.getLayoutParams();
						
						lastCellInfo.setLocation(CellInfo.CellLocation.HOTSEAT);
						lastCellInfo.setCellX(dragCellInfo.getCellX());
						lastCellInfo.setCellY(dragCellInfo.getCellY());
						lastCellInfo.setHotSeatCellX(dragCellInfo.getHotSeatCellX());
						lastCellInfo.setHotSeatCellY(dragCellInfo.getHotSeatCellY());
						
						lastObject.setItemVisibility(View.VISIBLE);
						hotSeat.removeView(dragObject.itemView);
						curCellLayout.removeView(lastObject.itemView);
						((ShortCutView2)lastObject.itemView).setLabelVisibility(View.GONE);
						hotSeat.addView(lastObject.itemView);
						hotSeat.flagOcuped(lastCellInfo);
						hotSeat.requestLayout();
						
						dbManager.updateDragInfo(lastCellInfo);
					}
					
					clearHintAnim();
				} else {
					Utils.log(TAG+"swap", "source=HotSeat, false, success=%b", success);
					
				}
				
			} else if (source instanceof Workspace) {
				if (success) {
					/*
					 * 1. 取消Hint动画-last swap item
					 * 2. last swap item 的 item view修改属性
					 * 3. 加入数据库
					 * 4. requestLayout
					 */
					Utils.log(TAG+"swap", "source=Workspace, true, success=%b", success);
					
					if (lastObject != null) {
						Utils.log(TAG+"swap", "lastObject not null");
						final CellInfo lastCellInfo = (CellInfo) lastObject.itemView.getTag();							
						final CellLayout.LayoutParams lp = (com.xx.mylauncher.CellLayout.LayoutParams) lastObject.itemView.getLayoutParams();
						
						lastCellInfo.setCellX(dragObject.preCellX);
						lastCellInfo.setCellY(dragObject.preCellY);
						
						lp.cellX = dragObject.preCellX;
						lp.cellY = dragObject.preCellY;
						lastObject.itemView.setLayoutParams(lp);
						lastObject.setItemVisibility(View.VISIBLE);
						curCellLayout.flagOcuped(lastCellInfo);
						curCellLayout.requestLayout();

						dbManager.updateDragInfo(lastCellInfo);
					}
					
					clearHintAnim();
				} else {
					Utils.log(TAG+"swap", "source=Workspace, false, success=%b", success);
					
				}
			}
			


		}
		
	}
	
	/**
	 * 交换的对象抽象
	 * @author baoxing
	 *
	 */
	static class SwapItemObject implements Cloneable{
		
		public boolean isInvalid = false;
		
		/** 动画开始的初始位置 */
		public int oriX, oriY;
		
		/** 动画运行的当前位置 */
		public int curX, curY;
		
		/**动画结束时/取消时 的位置 */
		public int finallyX, finallyY;
		
		public View itemView;
		
		public Bitmap bitmapItemView;
		
		/** 该对象所对应的动画 */
		public Animator animator;
		
		/**
		 * 设置item的可见性
		 * @param visibility
		 */
		public void setItemVisibility(int visibility) {
			this.itemView.setVisibility(visibility);
		}
		
		/**
		 * 是否同一个对象，通过比较itemView
		 * @param object
		 * @return
		 */
		public boolean isTheSameObject(final SwapItemObject object) {
			if (object == null) {
				return false;
			} else {
				return this.itemView == object.itemView;
			}
		}
		
		@Override
		protected Object clone() {
			SwapItemObject object = null;
			try {
				object = (SwapItemObject) super.clone();
				object.bitmapItemView = Utils.getViewBitmap(this.itemView);
				object.animator = null;
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			
			return object;
		}
		
		public void realase() {
			if (this.bitmapItemView != null) {
				this.bitmapItemView.recycle();
				this.bitmapItemView = null;
			}
		}

		@Override
		public String toString() {
			return "SwapItemObject [isInvalid=" + isInvalid + ", oriX=" + oriX
					+ ", oriY=" + oriY + ", curX=" + curX + ", curY=" + curY
					+ ", finallyX=" + finallyX + ", finallyY=" + finallyY
					+ ", itemView=" + itemView + ", bitmapItemView="
					+ bitmapItemView + ", animator=" + animator + "]";
		}
		
		
		
		
	}
	
	
	
	
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
