# encoding: UTF-8
# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 20150315200343) do

  create_table "groups", force: :cascade do |t|
    t.string   "name",                           null: false
    t.boolean  "public_group",    default: true, null: false
    t.string   "password_digest"
    t.string   "join_token"
    t.datetime "created_at",                     null: false
    t.datetime "updated_at",                     null: false
  end

  add_index "groups", ["join_token"], name: "index_groups_on_join_token"
  add_index "groups", ["name"], name: "index_groups_on_name"

  create_table "groups_users", id: false, force: :cascade do |t|
    t.integer "group_id"
    t.integer "user_id"
  end

  add_index "groups_users", ["group_id"], name: "index_groups_users_on_group_id"
  add_index "groups_users", ["user_id"], name: "index_groups_users_on_user_id"

  create_table "members", force: :cascade do |t|
    t.string   "email",           null: false
    t.string   "password_digest", null: false
    t.datetime "created_at",      null: false
    t.datetime "updated_at",      null: false
  end

  add_index "members", ["email"], name: "index_members_on_email"

  create_table "messages", force: :cascade do |t|
    t.string   "text",       null: false
    t.integer  "group_id",   null: false
    t.integer  "user_id"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
  end

  add_index "messages", ["group_id"], name: "index_messages_on_group_id"

  create_table "users", force: :cascade do |t|
    t.string   "name",            null: false
    t.string   "remember_digest"
    t.integer  "member_id"
    t.datetime "created_at",      null: false
    t.datetime "updated_at",      null: false
  end

  add_index "users", ["member_id"], name: "index_users_on_member_id"

end
