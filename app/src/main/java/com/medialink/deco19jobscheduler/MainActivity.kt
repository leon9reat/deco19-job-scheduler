package com.medialink.deco19jobscheduler

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val JOB_ID = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("debug", BuildConfig.API_KEY)

        btn_start.setOnClickListener(this)
        btn_cancel.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_start -> startJob()
            R.id.btn_cancel -> cancelJob()
        }
    }

    private fun cancelJob() {
        val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(JOB_ID)
        Toast.makeText(this, "job service canceled", Toast.LENGTH_SHORT).show()
    }

    private fun startJob() {
        if (isJobRunning(this)) {
            Toast.makeText(this, "job service is already scheduled", Toast.LENGTH_SHORT).show()
            return
        }

        val mServiceComponent = ComponentName(this, GetCurrentWeatherJobService::class.java)
        val builder = JobInfo.Builder(JOB_ID, mServiceComponent)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setRequiresDeviceIdle(false)
            .setRequiresCharging(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPeriodic(900000) // 15m
        } else {
            builder.setPeriodic(180000) // 3m
        }
        val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.schedule(builder.build())
        Toast.makeText(this, "job service started", Toast.LENGTH_SHORT).show()
    }

    private fun isJobRunning(context: Context): Boolean {
        var isScheduled = false
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        for (jobInfo in scheduler.allPendingJobs) {
            if (jobInfo.id == JOB_ID) {
                isScheduled = true
                break
            }
        }
        return isScheduled
    }


}
