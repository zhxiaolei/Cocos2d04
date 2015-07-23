package org.mobiletrain.cocos2d04;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cocos2d.actions.instant.CCCallFuncN;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor4B;

import android.view.MotionEvent;

public class GameLayer extends CCColorLayer {
	CGSize winSize = null;
	Random random = new Random();
	int bulletV = 480;

	List<CCSprite> bullets = new ArrayList<CCSprite>();
	List<CCSprite> targets = new ArrayList<CCSprite>();

	protected GameLayer(ccColor4B color) {
		super(color);
		setIsTouchEnabled(true);
		winSize = CCDirector.sharedDirector().displaySize();
		schedule("addTarget", 1f);
		schedule("check");// 0.02秒执行一次，但是不准
	}

	public void check(float delta) {
		List<CCSprite> deleteBullets = new ArrayList<CCSprite>();
		for (int i = 0; i < bullets.size(); i++) {
			CCSprite bullet = bullets.get(i);
			CGPoint pos = bullet.getPosition();
			CGSize size = bullet.getContentSize();
			CGRect bulletRect = CGRect.make(pos.x - size.width / 2, pos.y
					- size.height / 2, size.width, size.height);
			List<CCSprite> deleteTargets = new ArrayList<CCSprite>();

			for (int j = 0; j < targets.size(); j++) {
				CCSprite target = targets.get(j);
				CGPoint targetPos = target.getPosition();
				CGSize targetSize = target.getContentSize();
				CGRect targetRect = CGRect.make(targetPos.x - targetSize.width
						/ 2, targetPos.y - targetSize.height / 2,
						targetSize.width, targetSize.height);

				boolean b = CGRect.intersects(targetRect, bulletRect);
				if (b) {
					System.out.println("碰撞");
					deleteTargets.add(target);
					this.removeChild(target, true);
				}
			}
			if (deleteTargets.size() > 0) {
				targets.removeAll(deleteTargets);
				this.removeChild(bullet, true);
				deleteBullets.add(bullet);
			}
		}
		if (deleteBullets.size() > 0) {
			bullets.removeAll(deleteBullets);
		}

	}

	@Override
	public boolean ccTouchesBegan(MotionEvent event) {
		float x = event.getX();
		float y = winSize.height - event.getY();

		CCSprite bullet = CCSprite.sprite("bullet.png");
		CGSize bulletSize = bullet.getContentSize();
		bullet.setTag(2);
		bullets.add(bullet);
		this.addChild(bullet);
		float initX = bulletSize.width / 2;
		float initY = winSize.height / 2;
		CGPoint initPoint = CGPoint.ccp(initX, initY);
		bullet.setPosition(initPoint);

		float endX = winSize.width + bulletSize.width / 2;
		float endY = winSize.width * (y - initY) / (x - initX) + winSize.height
				/ 2;
		CGPoint endPoint = CGPoint.ccp(endX, endY);
		float distance = CGPoint.ccpDistance(initPoint, endPoint);
		float t = distance / bulletV;
		CCMoveTo moveTo = CCMoveTo.action(t, endPoint);
		CCCallFuncN func = CCCallFuncN.action(this, "onMoveFinished");
		CCSequence seq = CCSequence.actions(moveTo, func);
		bullet.runAction(seq);
		return super.ccTouchesBegan(event);
	}

	// 该方法用于向屏幕当中添加一个敌人
	public void addTarget(float delta) {
		// 生成一个目标精灵对象
		CCSprite targetSprite = CCSprite.sprite("target.png");
		this.addChild(targetSprite);
		targetSprite.setTag(1);
		targets.add(targetSprite);
		CGSize targetSize = targetSprite.getContentSize();
		float targetInitX = winSize.width + targetSize.width / 2;
		float targetInitY = random
				.nextInt((int) (winSize.height - targetSize.height))
				+ targetSize.height / 2;

		float targetEndX = -targetSize.width / 2;
		float targetEndY = targetInitY;

		CGPoint initPoint = CGPoint.ccp(targetInitX, targetInitY);
		CGPoint endPoint = CGPoint.ccp(targetEndX, targetEndY);
		targetSprite.setPosition(initPoint);

		float t = random.nextFloat() * 2 + 2;

		CCMoveTo moveTo = CCMoveTo.action(t, endPoint);
		CCCallFuncN func = CCCallFuncN.action(this, "onMoveFinished");
		CCSequence seq = CCSequence.actions(moveTo, func);
		targetSprite.runAction(seq);
	}

	public void onMoveFinished(Object sender) {
		CCSprite sprite = (CCSprite) sender;
		this.removeChild(sprite, true);
		if (sprite.getTag() == 1) {
			targets.remove(sprite);
		} else if (sprite.getTag() == 2) {
			bullets.remove(sprite);
		}
	}

}
