package cm;
import java.awt.*;
import robocode.*;
public class Seven extends AdvancedRobot
{
Enemy enemy = new Enemy();
public static double PI = Math.PI;
long lastScanTime = 0; // 添加一个变量来跟踪上一次扫描的时间

public void run()
{
setAdjustGunForRobotTurn( true );
setAdjustRadarForGunTurn( true );
this.setColors(Color.red, Color.blue, Color.yellow,
Color.black, Color.green);
setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
execute();
while(true) {
    // 执行移动和转向0
    smartMove();
    // 检查是否需要重新扫描
    if (getTime() - lastScanTime > 1) {
        // 如果一定时间内没有扫描到敌人，转一圈雷达
        setTurnRadarRightRadians(2 * PI);
    }
    execute();
}
}
public void smartMove() {
    // 根据敌人的位置和距离来决定移动策略
    double distance = enemy.distance; // 敌人的距离
    double angleToEnemy = getHeadingRadians() + enemy.bearingRadian; // 敌人的方向

    // 根据敌人的距离选择移动策略
    if(distance > 300) {
        // 如果敌人距离较远，向敌人靠近
        setTurnRightRadians(rectify(angleToEnemy - getHeadingRadians()));
        setAhead(distance / 2); // 向前移动一半的距离
    } else {
        // 如果敌人距离较近，执行蛇形移动
        setTurnRightRadians(rectify(angleToEnemy - getHeadingRadians() + PI/2));
        setAhead(150 * Math.signum(Math.sin(getTime() / 20))); // 根据时间变化执行蛇形移动
    }
}


public Point.Double predictFuturePosition(Enemy enemy, long time) {
    double futureX = enemy.x + Math.sin(enemy.headingRadian) * enemy.velocity * time;
    double futureY = enemy.y + Math.cos(enemy.headingRadian) * enemy.velocity * time;
    return new Point.Double(futureX, futureY);
}



public void onScannedRobot(ScannedRobotEvent e)
{
	lastScanTime = getTime();  
	enemy.update(e, this);
	 // 确定开火策略
	double firePower = getFirePower();
	double bulletSpeed = 20 - firePower * 3;
	long time = (long)(enemy.distance / bulletSpeed);
	    
	// 预测敌人未来位置
	double futureX = enemy.x + Math.sin(enemy.headingRadian) * enemy.velocity * time;
	double futureY = enemy.y + Math.cos(enemy.headingRadian) * enemy.velocity * time;
	double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);
	    
	// 调整炮塔方向并开火
	 setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));
	    if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
	        setFire(firePower);
	    }
	    
	    // 雷达锁定
	    double radarOffset = rectify(enemy.direction - getRadarHeadingRadians());
	    setTurnRadarRightRadians(radarOffset * 1.9);
}


public double getFirePower() {
    return Math.min(400 / enemy.distance, 3);
}

public double absoluteBearing(double x1, double y1, double x2, double y2) {
    double xo = x2-x1;
    double yo = y2-y1;
    double hyp = Point.distance(x1, y1, x2, y2);
    double arcSin = Math.toDegrees(Math.asin(xo / hyp));
    double bearing = 0;

    if (xo > 0 && yo > 0) { // both pos: lower-Left
        bearing = arcSin;
    } else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
        bearing = 360 + arcSin; // arcsin is negative here, actuall 360 - ang
    } else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
        bearing = 180 - arcSin;
    } else if (xo < 0 && yo < 0) { // both neg: upper-right
        bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
    }

    return bearing;
}

public double normalizeBearing(double angle) {
    while (angle >  180) angle -= 360;
    while (angle < -180) angle += 360;
    return angle;
}

// 坦克移动方法
public void onHitByBullet(HitByBulletEvent e) {
    // 当被子弹击中时，执行躲避动作
    setTurnRight(normalizeBearing(e.getBearing() + 90));
    setAhead(150);
}

//当机器人撞到墙壁时执行的动作
public void onHitWall(HitWallEvent e) {
 // 计算撞墙后的反向角度
 double bearing = e.getBearing(); // 获取撞墙的角度
 turnRight(-bearing); // 调整方向
 ahead(150); // 向前移动一段距离以远离墙壁
 // 在这里，您可以添加更多的逻辑来优化机器人的移动，例如根据当前位置和敌人的位置来决定移动方向。
}


//⻆度修正方法，重要
public double rectify ( double angle )
{
if ( angle < -Math.PI )
angle += 2*Math.PI;
if ( angle > Math.PI )
angle -= 2*Math.PI;
return angle;
}
public class Enemy {
public double x,y;
public String name = null;
public double headingRadian = 0.0D;
public double bearingRadian = 0.0D;
public double distance = 1000D;
public double direction = 0.0D;
public double velocity = 0.0D;
public double prevHeadingRadian = 0.0D;
public double energy = 100.0D;
public void update(ScannedRobotEvent
e,AdvancedRobot me){
name = e.getName();
headingRadian = e.getHeadingRadians();
bearingRadian = e.getBearingRadians();
this.energy = e.getEnergy();
this.velocity = e.getVelocity();
this.distance = e.getDistance();
direction = bearingRadian +
me.getHeadingRadians();
x = me.getX() + Math.sin( direction ) * distance;
y= me.getY() + Math.cos( direction ) * distance;
}
}
}