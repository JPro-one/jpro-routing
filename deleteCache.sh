#!/bin/bash

find ~/.m2 ~/.gradle | grep jpro-routing | xargs rm -r
find ~/.m2 ~/.gradle | grep jpro-auth | xargs rm -r