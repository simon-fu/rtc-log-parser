#!/usr/bin/python
# -*- coding: UTF-8 -*- 

import re
import sys
import getopt

# 使用方法： python conference_analyser.py -i failed_recon.log -o result.txt
# TODO： 分析多个输入文件，生成完整的会议记录
#

CREATE = re.compile('createAndJoinConference')

JOIN = re.compile('Join conference success')
PUBLISH = re.compile('Publish success Stream')
OPEN_MIC = re.compile('open mic success')

ICE_CONNECTED = re.compile('onIceConnected')
FIRST_FRAME = re.compile('onFirstFrameAvailable')

DESTROY = re.compile('destroyConference')
REPORT = re.compile('ReportString')

MEMBER_JOIN = re.compile('onMemberJoined()')
MEMBER_EXIT = re.compile('onMemberExited()')
STREAM_ADDED = re.compile('onStreamAdded()')
STREAM_REMOVED = re.compile('onStreamRemoved()')
SUB_STREAM = re.compile('Subscribe success Stream id')

SIGNAL_STATE_TRANSITION = re.compile('exec state session:')
STREAM_STATE_TRANSITION = re.compile('exec state rtc:')
NETWORK_CHANGED = re.compile('Network changed')

SIGNAL_UPLINK = re.compile('signal msg: ==>')
SIGNAL_DOWNLINK = re.compile('signal msg: <==')


def main(argv):
    inputfile = ''
    outputfile = ''

    try:
        opts, args = getopt.getopt(argv[1:], "-h-i:-o:", ["ifile=", "ofile="])

    except getopt.GetoptError:
        print 'test.py -i <inputfile> -o <outputfile>'
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print 'test.py -i <inputfile> -o <outputfile>'
            sys.exit()
        elif opt in ("-i", "--ifile"):
            inputfile = arg
            print 'input file: ', inputfile
            parser(inputfile)
        elif opt in ("-o", "--ofile"):
            outputfile = arg


def parser(file):
    log_file = open(file, 'r')
    print 'start parse file'

    for line in log_file:
        if CREATE.search(line):
            print line
        if JOIN.search(line):
            print line
        if PUBLISH.search(line):
            print line
        if MEMBER_JOIN.search(line):
            print line
        if MEMBER_EXIT.search(line):
            print line
        if STREAM_ADDED.search(line):
            print line
        if STREAM_REMOVED.search(line):
            print line
        if SUB_STREAM.search(line):
            print line
        if ICE_CONNECTED.search(line):
            print line
        if OPEN_MIC.search(line):
            print line
        if FIRST_FRAME.search(line):
            print line
        if DESTROY.search(line):
            print line
        if REPORT.search(line):
            print line

    print 'SIGNAL_STATE_TRANSITION'

    log_file.seek(0)
    for line in log_file:
        if NETWORK_CHANGED.search(line):
            print line
        if SIGNAL_STATE_TRANSITION.search(line):
            print line

    print 'STREAM_STATE_TRANSITION'

    log_file.seek(0)
    for line in log_file:
        if NETWORK_CHANGED.search(line):
            print line
        if STREAM_STATE_TRANSITION.search(line):
            print line

    print 'SIGNAL MESSAGE:'

    log_file.seek(0)
    for line in log_file:
        if SIGNAL_UPLINK.search(line):
            print line
        if SIGNAL_DOWNLINK.search(line):
            print line


    log_file.close()


if __name__ == "__main__":
    main(sys.argv)
